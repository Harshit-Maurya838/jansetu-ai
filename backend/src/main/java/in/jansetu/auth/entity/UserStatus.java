package in.jansetu.auth.entity;

/** Account lifecycle. Admin-created users start PENDING_ACTIVATION until they set a password via OTP. */
public enum UserStatus {
    PENDING_ACTIVATION,
    ACTIVE,
    LOCKED,
    DISABLED
}
