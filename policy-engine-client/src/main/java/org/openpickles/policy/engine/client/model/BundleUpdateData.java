package org.openpickles.policy.engine.client.model;

public class BundleUpdateData {
    private String bundleName;
    private String version;
    private String downloadUrl;

    public BundleUpdateData() {
    }

    public BundleUpdateData(String bundleName, String version, String downloadUrl) {
        this.bundleName = bundleName;
        this.version = version;
        this.downloadUrl = downloadUrl;
    }

    public String getBundleName() {
        return bundleName;
    }

    public void setBundleName(String bundleName) {
        this.bundleName = bundleName;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }
}
