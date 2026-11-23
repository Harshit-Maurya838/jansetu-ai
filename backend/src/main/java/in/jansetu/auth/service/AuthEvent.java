package in.jansetu.auth.service;

/** Values written to audit_logs.action (entity_type = USER). */
public enum AuthEvent {
    LOGIN_SUCCESS,
    LOGIN_FAILED,
    ACCOUNT_LOCKED,
    OTP_SENT,
    PASSWORD_RESET,
    PASSWORD_CHANGED,
    ACCOUNT_ACTIVATED,
    LOGOUT,
    LOGOUT_ALL,
    SESSION_REVOKED,
    TOKEN_REUSE_DETECTED,
    USER_CREATED,
    USER_UPDATED,
    ROLE_CHANGED,
    USER_STATUS_CHANGED
}
