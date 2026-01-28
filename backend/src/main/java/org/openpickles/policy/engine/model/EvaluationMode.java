package org.openpickles.policy.engine.model;

public enum EvaluationMode {
    DIRECT,
    RBAC_ONLY,
    PBC_CHAIN,
    ATTRIBUTE,
    CONDITION,
    ALL_MUST_ALLOW
}
