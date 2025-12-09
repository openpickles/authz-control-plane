package org.openpickles.policy.engine.exception;

public class FunctionalException extends PolicyEngineException {

    public FunctionalException(String message, String errorCode) {
        super(message, errorCode, false);
    }

    public FunctionalException(String message, String errorCode, Throwable cause) {
        super(message, errorCode, false, cause);
    }
}
