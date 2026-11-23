package in.jansetu.auth.dto;

/** Shared Bean Validation patterns for auth DTOs. */
public final class Validation {
    /** Indian mobile, with or without +91; normalised to E.164 by the service. */
    public static final String MOBILE = "^(\\+91)?[6-9]\\d{9}$";
    public static final String MOBILE_MSG = "must be a 10-digit Indian mobile number";
    public static final String OTP = "^\\d{6}$";
    public static final String LANGUAGE = "^(en|hi)$";

    private Validation() {
    }
}
