package org.openpickles.policy.engine.service;

import org.openpickles.policy.engine.dto.request.ManifestSyncRequest;
import org.openpickles.policy.engine.dto.manifest.*;
import org.openpickles.policy.engine.model.*;
import org.openpickles.policy.engine.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class SyncService {

    @Autowired
    private ServiceRegistryRepository serviceRegistryRepository;
    @Autowired
    private ResourceTypeRepository resourceTypeRepository;
    @Autowired
    private PolicyRepository policyRepository;
    @Autowired
    private PolicyBindingRepository policyBindingRepository;
    @Autowired
    private PolicyBundleRepository policyBundleRepository;

    @Transactional
    public void processManifest(ManifestSyncRequest request) {
        PolicyManifest manifest = request.getManifest();
        ServiceInfo serviceInfo = manifest.getService();
        String serviceName = serviceInfo.getName();
        String currentHash = request.getManifestHash();

        // 1. Idempotency Check (with Pessimistic Lock)
        Optional<ServiceRegistry> existingService = serviceRegistryRepository.findByNameForUpdate(serviceName);
        if (existingService.isPresent()) {
            ServiceRegistry service = existingService.get();
            if (currentHash != null && currentHash.equals(service.getLastSyncHash())) {
                // Already synced this version/hash, but ensure public key is up to date just in
                // case
                if (serviceInfo.getPublicKey() != null && !serviceInfo.getPublicKey().equals(service.getPublicKey())) {
                    service.setPublicKey(serviceInfo.getPublicKey());
                    serviceRegistryRepository.save(service);
                }
                return;
            }
            // Update metadata
            service.setVersion(serviceInfo.getVersion());
            service.setDescription(serviceInfo.getDescription());
            service.setLastSyncHash(currentHash);
            service.setLastSyncTime(LocalDateTime.now());
            // Client sync always updates public key if provided
            if (serviceInfo.getPublicKey() != null) {
                service.setPublicKey(serviceInfo.getPublicKey());
            }
            // Ensure mode is set if missing (migration)
            if (service.getRegistrationMode() == null) {
                service.setRegistrationMode(ServiceRegistry.RegistrationMode.CLIENT);
                service.setImmutable(true);
            }
            serviceRegistryRepository.save(service);
        } else {
            ServiceRegistry newService = new ServiceRegistry();
            newService.setName(serviceName);
            newService.setVersion(serviceInfo.getVersion());
            newService.setDescription(serviceInfo.getDescription());
            newService.setLastSyncHash(currentHash);
            newService.setLastSyncTime(LocalDateTime.now());
            newService.setPublicKey(serviceInfo.getPublicKey());
            newService.setRegistrationMode(ServiceRegistry.RegistrationMode.CLIENT);
            newService.setImmutable(true);
            serviceRegistryRepository.save(newService);
        }

        // 2. Upsert Resource Types
        if (manifest.getResourceTypes() != null) {
            for (ResourceTypeDefinition def : manifest.getResourceTypes()) {
                upsertResourceType(def);
            }
        }

        // 3. Upsert Policies (PRODUCT layer)
        if (manifest.getPolicies() != null) {
            for (PolicyDefinition policyDef : manifest.getPolicies()) {
                upsertPolicy(policyDef, serviceName);
            }
        }

        // 4. Upsert Bindings & Bundles
        // The manifest defines 'bundles'. Each bundle has 'contexts'.
        // Bindings link context + resourceType -> policies.
        // We need to create PolicyBinding entities first, then Bundles linking them.

        // This mapping is complex because BundleDefinition in manifest just lists
        // contexts.
        // But PolicyBundle in backend links to Binding IDs.
        // So we need to create bindings for all definitions.

        List<Long> allBindingIdsForService = new ArrayList<>();

        if (manifest.getBindings() != null) {
            for (BindingDefinition bindingDef : manifest.getBindings()) {
                Long bindingId = upsertBinding(bindingDef, serviceName);
                if (bindingId != null) {
                    allBindingIdsForService.add(bindingId);
                }
            }
        }

        // 5. Upsert Bundles
        // Manifest bundles imply grouping of bindings by context?
        // "Bundles: [ { name: 'default', contexts: ['http'] } ]"
        // So for each BundleDefinition, we find all bindings (created above) that match
        // the contexts.

        if (manifest.getBundles() != null) {
            // Re-fetch service to ensure we have the latest state (and session)
            ServiceRegistry currentService = serviceRegistryRepository.findByName(serviceName).orElseThrow();

            for (BundleDefinition bundleDef : manifest.getBundles()) {
                upsertBundle(bundleDef, currentService);
            }
        }
    }

    private void upsertResourceType(ResourceTypeDefinition def) {
        String key = def.getKey() != null ? def.getKey() : def.getName();
        Optional<ResourceType> existing = resourceTypeRepository.findByKey(key);
        ResourceType rt = existing.orElse(new ResourceType());
        rt.setKey(key);
        rt.setName(def.getName());
        rt.setDescription(def.getDescription());
        resourceTypeRepository.save(rt);
    }

    private void upsertPolicy(PolicyDefinition def, String serviceName) {
        Optional<Policy> existing = policyRepository.findByNameAndServiceOwnerAndOrigin(
                def.getName(), serviceName, Policy.PolicyOrigin.PRODUCT);

        Policy policy = existing.orElse(new Policy());
        policy.setName(def.getName());
        policy.setServiceOwner(serviceName);
        policy.setOrigin(Policy.PolicyOrigin.PRODUCT);
        policy.setDescription(def.getDescription());
        policy.setFilename(def.getFile());
        policy.setContent(def.getContent());
        policy.setStatus(Policy.PolicyStatus.ACTIVE);
        policy.setSourceType(Policy.SourceType.MANUAL);
        policy.setLastSyncTime(LocalDateTime.now());

        policyRepository.save(policy);
    }

    private Long upsertBinding(BindingDefinition def, String serviceName) {
        String contextKey = def.getContext();
        String resourceKey = def.getResourceType();

        Optional<PolicyBinding> existing = policyBindingRepository.findByResourceTypeAndContextAndServiceOwner(
                resourceKey, contextKey, serviceName);

        PolicyBinding binding = existing.orElse(new PolicyBinding());
        binding.setResourceType(resourceKey);
        binding.setContext(contextKey);
        binding.setServiceOwner(serviceName);
        binding.setEvaluationMode(EvaluationMode.DIRECT);
        String modeStr = def.getEvaluationMode() != null ? def.getEvaluationMode() : def.getMode();
        if (modeStr != null) {
            try {
                binding.setEvaluationMode(EvaluationMode.valueOf(modeStr.toUpperCase()));
            } catch (Exception e) {
                // Ignore unknown modes
            }
        }

        List<Long> policyIds = new ArrayList<>();
        if (def.getPolicies() != null) {
            for (String policyName : def.getPolicies()) {
                Optional<Policy> p = policyRepository.findByNameAndServiceOwnerAndOrigin(
                        policyName, serviceName, Policy.PolicyOrigin.PRODUCT);
                p.ifPresent(val -> policyIds.add(val.getId()));
            }
        }
        binding.setPolicyIds(policyIds); // Corrected typo

        return policyBindingRepository.save(binding).getId();
    }

    private void upsertBundle(BundleDefinition def, ServiceRegistry service) {
        String serviceName = service.getName();
        Optional<PolicyBundle> existing = policyBundleRepository.findByName(def.getName());

        PolicyBundle bundle;
        if (existing.isPresent()) {
            bundle = existing.get();
            // Check ownership
            if (serviceName.equals(bundle.getServiceOwner())) {
                // I am the Owner -> Update Definition
                updateBundleDetails(bundle, def, serviceName);
                policyBundleRepository.save(bundle);
            } else {
                // I am NOT the Owner -> Just Subscribe
                // Log warning if definition differs?
            }
        } else {
            // New Bundle -> Create and Claim Ownership
            bundle = new PolicyBundle();
            bundle.setName(def.getName());
            bundle.setServiceOwner(serviceName);
            bundle.setOrigin(Policy.PolicyOrigin.PRODUCT);
            updateBundleDetails(bundle, def, serviceName);
            policyBundleRepository.save(bundle);
        }

        // Add subscription if not present
        if (!service.getSubscribedBundles().contains(bundle)) {
            service.getSubscribedBundles().add(bundle);
            serviceRegistryRepository.save(service);
        }
    }

    private void updateBundleDetails(PolicyBundle bundle, BundleDefinition def, String serviceName) {
        bundle.setTargetService(def.getTargetService());
        bundle.setRefreshInterval(def.getRefreshInterval());

        // Find Bindings matching contexts (Scoped to this service, since we are
        // defining it)
        List<Long> bindingIds = new ArrayList<>();
        if (def.getContexts() != null) {
            for (String ctx : def.getContexts()) {
                List<PolicyBinding> bindings = policyBindingRepository.findByContextAndServiceOwner(ctx, serviceName);
                for (PolicyBinding b : bindings) {
                    bindingIds.add(b.getId());
                }
            }
        }
        bundle.setBindingIds(bindingIds);
    }
}
