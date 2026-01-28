package org.openpickles.policy.engine.client;

public class ClientConfig {
    private String controlPlaneUrl;
    private String bundleName;
    private String opaUrl;
    private boolean autoUpdateOpa;
    private boolean failFast;
    private String transportType = "WEBSOCKET";
    private String kafkaBootstrapServers;
    private String kafkaTopic;
    private String kafkaGroupId;
    private String rabbitHost;
    private int rabbitPort;
    private String rabbitUsername;
    private String rabbitPassword;
    private String rabbitExchange;
    private String authHeader; // E.g. "Basic ..." or "Bearer ..."
    private String clientId;
    private String clientSecret;
    private String tokenUri;
    private String manifestPath;
    private String publicKeyPath;
    private long retryInitialInterval = 2000;
    private long retryMaxInterval = 60000;
    private double retryMultiplier = 2.0;

    public ClientConfig() {
    }

    // Builder Pattern
    public static class Builder {
        private String controlPlaneUrl;
        private String bundleName;
        private String opaUrl;
        private boolean autoUpdateOpa;
        private boolean failFast = false;
        private String transportType = "WEBSOCKET";
        private String kafkaBootstrapServers;
        private String kafkaTopic;
        private String kafkaGroupId;
        private String rabbitHost;
        private int rabbitPort;
        private String rabbitUsername;
        private String rabbitPassword;
        private String rabbitExchange;
        private String authHeader;
        private String clientId;
        private String clientSecret;
        private String tokenUri;
        private String manifestPath;
        private String publicKeyPath;
        private long retryInitialInterval = 2000;
        private long retryMaxInterval = 60000;
        private double retryMultiplier = 2.0;

        public Builder failFast(boolean failFast) {
            this.failFast = failFast;
            return this;
        }

        public Builder controlPlaneUrl(String controlPlaneUrl) {
            this.controlPlaneUrl = controlPlaneUrl;
            return this;
        }

        public Builder bundleName(String bundleName) {
            this.bundleName = bundleName;
            return this;
        }

        public Builder opaUrl(String opaUrl) {
            this.opaUrl = opaUrl;
            return this;
        }

        public Builder autoUpdateOpa(boolean autoUpdateOpa) {
            this.autoUpdateOpa = autoUpdateOpa;
            return this;
        }

        public Builder transportType(String transportType) {
            this.transportType = transportType;
            return this;
        }

        public Builder kafkaBootstrapServers(String kafkaBootstrapServers) {
            this.kafkaBootstrapServers = kafkaBootstrapServers;
            return this;
        }

        public Builder kafkaTopic(String kafkaTopic) {
            this.kafkaTopic = kafkaTopic;
            return this;
        }

        public Builder kafkaGroupId(String kafkaGroupId) {
            this.kafkaGroupId = kafkaGroupId;
            return this;
        }

        public Builder rabbitHost(String rabbitHost) {
            this.rabbitHost = rabbitHost;
            return this;
        }

        public Builder rabbitPort(int rabbitPort) {
            this.rabbitPort = rabbitPort;
            return this;
        }

        public Builder rabbitUsername(String rabbitUsername) {
            this.rabbitUsername = rabbitUsername;
            return this;
        }

        public Builder rabbitPassword(String rabbitPassword) {
            this.rabbitPassword = rabbitPassword;
            return this;
        }

        public Builder rabbitExchange(String rabbitExchange) {
            this.rabbitExchange = rabbitExchange;
            return this;
        }

        public Builder authHeader(String authHeader) {
            this.authHeader = authHeader;
            return this;
        }

        public Builder clientId(String clientId) {
            this.clientId = clientId;
            return this;
        }

        public Builder clientSecret(String clientSecret) {
            this.clientSecret = clientSecret;
            return this;
        }

        public Builder tokenUri(String tokenUri) {
            this.tokenUri = tokenUri;
            return this;
        }

        public Builder manifestPath(String manifestPath) {
            this.manifestPath = manifestPath;
            return this;
        }

        public Builder publicKeyPath(String publicKeyPath) {
            this.publicKeyPath = publicKeyPath;
            return this;
        }

        public Builder retryInitialInterval(long retryInitialInterval) {
            this.retryInitialInterval = retryInitialInterval;
            return this;
        }

        public Builder retryMaxInterval(long retryMaxInterval) {
            this.retryMaxInterval = retryMaxInterval;
            return this;
        }

        public Builder retryMultiplier(double retryMultiplier) {
            this.retryMultiplier = retryMultiplier;
            return this;
        }

        public ClientConfig build() {
            ClientConfig config = new ClientConfig();
            config.controlPlaneUrl = this.controlPlaneUrl;
            config.bundleName = this.bundleName;
            config.opaUrl = this.opaUrl;
            config.autoUpdateOpa = this.autoUpdateOpa;
            config.transportType = this.transportType;
            config.kafkaBootstrapServers = this.kafkaBootstrapServers;
            config.kafkaTopic = this.kafkaTopic;
            config.kafkaGroupId = this.kafkaGroupId;
            config.rabbitHost = this.rabbitHost;
            config.rabbitPort = this.rabbitPort;
            config.rabbitUsername = this.rabbitUsername;
            config.rabbitPassword = this.rabbitPassword;
            config.rabbitPassword = this.rabbitPassword;
            config.rabbitExchange = this.rabbitExchange;
            config.authHeader = this.authHeader;
            config.clientId = this.clientId;
            config.clientSecret = this.clientSecret;
            config.tokenUri = this.tokenUri;
            config.manifestPath = this.manifestPath;
            config.publicKeyPath = this.publicKeyPath;
            config.retryInitialInterval = this.retryInitialInterval;
            config.retryMaxInterval = this.retryMaxInterval;
            config.retryMultiplier = this.retryMultiplier;
            config.failFast = this.failFast;
            return config;
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getControlPlaneUrl() {
        return controlPlaneUrl;
    }

    public void setControlPlaneUrl(String controlPlaneUrl) {
        this.controlPlaneUrl = controlPlaneUrl;
    }

    public String getBundleName() {
        return bundleName;
    }

    public void setBundleName(String bundleName) {
        this.bundleName = bundleName;
    }

    public String getOpaUrl() {
        return opaUrl;
    }

    public void setOpaUrl(String opaUrl) {
        this.opaUrl = opaUrl;
    }

    public boolean isAutoUpdateOpa() {
        return autoUpdateOpa;
    }

    public void setAutoUpdateOpa(boolean autoUpdateOpa) {
        this.autoUpdateOpa = autoUpdateOpa;
    }

    public boolean isFailFast() {
        return failFast;
    }

    public void setFailFast(boolean failFast) {
        this.failFast = failFast;
    }

    public String getTransportType() {
        return transportType;
    }

    public void setTransportType(String transportType) {
        this.transportType = transportType;
    }

    public String getKafkaBootstrapServers() {
        return kafkaBootstrapServers;
    }

    public void setKafkaBootstrapServers(String kafkaBootstrapServers) {
        this.kafkaBootstrapServers = kafkaBootstrapServers;
    }

    public String getKafkaTopic() {
        return kafkaTopic;
    }

    public void setKafkaTopic(String kafkaTopic) {
        this.kafkaTopic = kafkaTopic;
    }

    public String getKafkaGroupId() {
        return kafkaGroupId;
    }

    public void setKafkaGroupId(String kafkaGroupId) {
        this.kafkaGroupId = kafkaGroupId;
    }

    public String getRabbitHost() {
        return rabbitHost;
    }

    public void setRabbitHost(String rabbitHost) {
        this.rabbitHost = rabbitHost;
    }

    public int getRabbitPort() {
        return rabbitPort;
    }

    public void setRabbitPort(int rabbitPort) {
        this.rabbitPort = rabbitPort;
    }

    public String getRabbitUsername() {
        return rabbitUsername;
    }

    public void setRabbitUsername(String rabbitUsername) {
        this.rabbitUsername = rabbitUsername;
    }

    public String getRabbitPassword() {
        return rabbitPassword;
    }

    public void setRabbitPassword(String rabbitPassword) {
        this.rabbitPassword = rabbitPassword;
    }

    public String getRabbitExchange() {
        return rabbitExchange;
    }

    public void setRabbitExchange(String rabbitExchange) {
        this.rabbitExchange = rabbitExchange;
    }

    public String getAuthHeader() {
        return authHeader;
    }

    public void setAuthHeader(String authHeader) {
        this.authHeader = authHeader;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

    public String getTokenUri() {
        return tokenUri;
    }

    public void setTokenUri(String tokenUri) {
        this.tokenUri = tokenUri;
    }

    public String getManifestPath() {
        return manifestPath;
    }

    public void setManifestPath(String manifestPath) {
        this.manifestPath = manifestPath;
    }

    public String getPublicKeyPath() {
        return publicKeyPath;
    }

    public void setPublicKeyPath(String publicKeyPath) {
        this.publicKeyPath = publicKeyPath;
    }

    public long getRetryInitialInterval() {
        return retryInitialInterval;
    }

    public void setRetryInitialInterval(long retryInitialInterval) {
        this.retryInitialInterval = retryInitialInterval;
    }

    public long getRetryMaxInterval() {
        return retryMaxInterval;
    }

    public void setRetryMaxInterval(long retryMaxInterval) {
        this.retryMaxInterval = retryMaxInterval;
    }

    public double getRetryMultiplier() {
        return retryMultiplier;
    }

    public void setRetryMultiplier(double retryMultiplier) {
        this.retryMultiplier = retryMultiplier;
    }
}
