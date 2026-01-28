package org.openpickles.policy.engine.client.auth;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NoOpTokenProvider implements TokenProvider {
    private static final Logger log = LoggerFactory.getLogger(NoOpTokenProvider.class);

    @Override
    public String getAccessToken() {
        log.debug("NoOpTokenProvider: Returning null (no auth)");
        return null;
    }
}
