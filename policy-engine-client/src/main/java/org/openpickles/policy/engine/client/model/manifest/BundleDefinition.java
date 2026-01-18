package org.openpickles.policy.engine.client.model.manifest;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;

public class BundleDefinition {
    private String name;

    private String refreshInterval;

    private String targetService;

    private List<String> contexts;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRefreshInterval() {
        return refreshInterval;
    }

    public void setRefreshInterval(String refreshInterval) {
        this.refreshInterval = refreshInterval;
    }

    public String getTargetService() {
        return targetService;
    }

    public void setTargetService(String targetService) {
        this.targetService = targetService;
    }

    public List<String> getContexts() {
        return contexts;
    }

    public void setContexts(List<String> contexts) {
        this.contexts = contexts;
    }
}
