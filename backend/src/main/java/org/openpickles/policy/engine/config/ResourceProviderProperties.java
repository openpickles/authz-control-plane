package org.openpickles.policy.engine.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Configuration
@ConfigurationProperties(prefix = "policy-engine")
public class ResourceProviderProperties {

    private List<DefaultProvider> defaultProviders = new ArrayList<>();

    public List<DefaultProvider> getDefaultProviders() {
        return defaultProviders;
    }

    public void setDefaultProviders(List<DefaultProvider> defaultProviders) {
        this.defaultProviders = defaultProviders;
    }

    public static class DefaultProvider {
        private String serviceName;
        private String baseUrl;
        private String resourceType;
        private String fetchEndpoint;

        public String getServiceName() {
            return serviceName;
        }

        public void setServiceName(String serviceName) {
            this.serviceName = serviceName;
        }

        public String getBaseUrl() {
            return baseUrl;
        }

        public void setBaseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
        }

        public String getResourceType() {
            return resourceType;
        }

        public void setResourceType(String resourceType) {
            this.resourceType = resourceType;
        }

        public String getFetchEndpoint() {
            return fetchEndpoint;
        }

        public void setFetchEndpoint(String fetchEndpoint) {
            this.fetchEndpoint = fetchEndpoint;
        }
    }
}
