package org.openpickles.policy.engine.dto.request;

import org.openpickles.policy.engine.dto.manifest.PolicyManifest;

public class ManifestSyncRequest {
    private PolicyManifest manifest;
    private String manifestHash;

    public PolicyManifest getManifest() {
        return manifest;
    }

    public void setManifest(PolicyManifest manifest) {
        this.manifest = manifest;
    }

    public String getManifestHash() {
        return manifestHash;
    }

    public void setManifestHash(String manifestHash) {
        this.manifestHash = manifestHash;
    }
}
