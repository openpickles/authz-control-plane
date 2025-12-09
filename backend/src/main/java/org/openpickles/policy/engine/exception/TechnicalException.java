package org.openpickles.policy.engine.exception;

public class TechnicalException extends PolicyEngineException {

    public TechnicalException(String message, String errorCode) {
        super(message, errorCode, true);
    }

    public TechnicalException(String message, String errorCode, Throwable cause) {
        super(message, errorCode, true, cause);
    }
}
