package org.openpickles.policy.engine.controller;

import org.openpickles.policy.engine.dto.ServiceRegistryDTO;
import org.openpickles.policy.engine.model.ServiceRegistry;
import org.openpickles.policy.engine.repository.ServiceRegistryRepository;
import org.openpickles.policy.engine.repository.PolicyRepository;
import org.openpickles.policy.engine.model.Policy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/services")
public class ServiceRegistryController {

    @Autowired
    private ServiceRegistryRepository serviceRegistryRepository;

    @Autowired
    private PolicyRepository policyRepository;

    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(ServiceRegistryController.class);

    @GetMapping
    public ResponseEntity<List<ServiceRegistryDTO>> getAllServices() {
        try {
            List<ServiceRegistry> services = serviceRegistryRepository.findAll();

            List<ServiceRegistryDTO> dtos = services.stream().map(service -> {
                ServiceRegistryDTO dto = new ServiceRegistryDTO();
                dto.setId(service.getId());
                dto.setName(service.getName());
                dto.setVersion(service.getVersion());
                dto.setDescription(service.getDescription());
                dto.setLastSyncTime(service.getLastSyncTime());
                dto.setLastSyncHash(service.getLastSyncHash());
                dto.setPublicKey(service.getPublicKey());
                dto.setRegistrationMode(
                        service.getRegistrationMode() != null ? service.getRegistrationMode().name() : "UNKNOWN");
                dto.setImmutable(service.isImmutable());

                try {
                    // Calculate Status
                    long customCount = policyRepository.countByServiceOwnerAndOrigin(service.getName(),
                            Policy.PolicyOrigin.CUSTOM);
                    dto.setCustomPolicyCount(customCount);
                    // Populate Bundle Count (Owned + Subscribed)
                    dto.setBundleCount(
                            service.getSubscribedBundles() != null ? service.getSubscribedBundles().size() : 0);

                    if (service.getLastSyncTime() == null
                            && service.getRegistrationMode() == ServiceRegistry.RegistrationMode.MANUAL) {
                        dto.setStatus("REGISTERED");
                    } else if (service.getLastSyncTime() == null) {
                        dto.setStatus("UNKNOWN");
                    } else if (service.getLastSyncTime().isBefore(LocalDateTime.now().minusHours(1))) {
                        dto.setStatus("OFFLINE");
                    } else if (customCount > 0) {
                        dto.setStatus("MODIFIED");
                    } else {
                        dto.setStatus("HEALTHY");
                    }
                } catch (Exception e) {
                    logger.error("Error calculating status for service: {}", service.getName(), e);
                    dto.setStatus("UNKNOWN"); // Fallback
                }

                return dto;
            }).collect(Collectors.toList());

            return ResponseEntity.ok(dtos);
        } catch (Exception e) {
            logger.error("Failed to fetch services", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
