package org.openpickles.policy.engine.exception;

public abstract class PolicyEngineException extends RuntimeException {
    private final String errorCode;
    private final boolean isTechnical;

    public String getErrorCode() {
        return errorCode;
    }

    public boolean isTechnical() {
        return isTechnical;
    }

    protected PolicyEngineException(String message, String errorCode, boolean isTechnical) {
        super(message);
        this.errorCode = errorCode;
        this.isTechnical = isTechnical;
    }

    protected PolicyEngineException(String message, String errorCode, boolean isTechnical, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.isTechnical = isTechnical;
    }
}
