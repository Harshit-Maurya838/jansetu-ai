package in.jansetu.auth.dto;

/** Same body whether or not the account exists (no account enumeration). */
public record OtpSentResponse(String message, long resendAfterSeconds) {
    public static final OtpSentResponse GENERIC =
            new OtpSentResponse("If the details are valid, a code has been sent", 60);
}
