package org.openpickles.policy.engine.client.auth;

public interface TokenProvider {
    /**
     * returns a valid access token (Bearer token value only), or null if no auth is
     * needed.
     */
    String getAccessToken();
}
