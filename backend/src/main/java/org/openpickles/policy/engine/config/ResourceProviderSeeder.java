package org.openpickles.policy.engine.config;

import org.openpickles.policy.engine.model.ResourceProvider;
import org.openpickles.policy.engine.repository.ResourceProviderRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ResourceProviderSeeder implements CommandLineRunner {

    private final ResourceProviderProperties properties;
    private final ResourceProviderRepository repository;

    public ResourceProviderSeeder(ResourceProviderProperties properties, ResourceProviderRepository repository) {
        this.properties = properties;
        this.repository = repository;
    }

    @Override
    public void run(String... args) throws Exception {
        List<ResourceProviderProperties.DefaultProvider> defaults = properties.getDefaultProviders();

        if (defaults == null || defaults.isEmpty()) {
            return;
        }

        for (ResourceProviderProperties.DefaultProvider def : defaults) {
            // Check if provider with this service name already exists
            boolean exists = repository.findAll().stream()
                    .anyMatch(p -> p.getServiceName().equals(def.getServiceName()));

            if (!exists) {
                ResourceProvider provider = new ResourceProvider();
                provider.setServiceName(def.getServiceName());
                provider.setBaseUrl(def.getBaseUrl());
                provider.setResourceType(def.getResourceType());
                provider.setFetchEndpoint(def.getFetchEndpoint());

                repository.save(provider);
                System.out.println("Seeded default resource provider: " + def.getServiceName());
            }
        }
    }
}
