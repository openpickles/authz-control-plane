package org.openpickles.policy.engine.client.model.sync;

import org.openpickles.policy.engine.client.model.manifest.ClientManifest;

public class ManifestSyncRequest {
    private ClientManifest manifest;
    private String manifestHash;

    public ManifestSyncRequest() {
    }

    public ManifestSyncRequest(ClientManifest manifest, String manifestHash) {
        this.manifest = manifest;
        this.manifestHash = manifestHash;
    }

    public ClientManifest getManifest() {
        return manifest;
    }

    public void setManifest(ClientManifest manifest) {
        this.manifest = manifest;
    }

    public String getManifestHash() {
        return manifestHash;
    }

    public void setManifestHash(String manifestHash) {
        this.manifestHash = manifestHash;
    }
}
