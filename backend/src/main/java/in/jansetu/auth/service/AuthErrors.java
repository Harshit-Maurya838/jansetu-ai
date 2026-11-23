package in.jansetu.auth.service;

import in.jansetu.common.exception.ApiException;
import org.springframework.http.HttpStatus;

/** Error codes the mobile app translates (docs/modules/01-authentication.md#error-codes). */
public final class AuthErrors {
    private AuthErrors() {
    }

    public static ApiException invalidCredentials() {
        return new ApiException(HttpStatus.UNAUTHORIZED, "AUTH_INVALID_CREDENTIALS", "Invalid login details");
    }

    public static ApiException accountLocked() {
        return new ApiException(HttpStatus.LOCKED, "AUTH_ACCOUNT_LOCKED", "Too many failed attempts. Try again later");
    }

    public static ApiException accountDisabled() {
        return new ApiException(HttpStatus.FORBIDDEN, "AUTH_ACCOUNT_DISABLED", "This account is disabled");
    }

    public static ApiException activationRequired() {
        return new ApiException(HttpStatus.FORBIDDEN, "AUTH_ACTIVATION_REQUIRED", "Activate your account first");
    }

    public static ApiException otpInvalid() {
        return new ApiException(HttpStatus.BAD_REQUEST, "AUTH_OTP_INVALID", "The code is incorrect");
    }

    public static ApiException otpExpired() {
        return new ApiException(HttpStatus.BAD_REQUEST, "AUTH_OTP_EXPIRED", "The code has expired");
    }

    public static ApiException otpAttemptsExceeded() {
        return new ApiException(HttpStatus.BAD_REQUEST, "AUTH_OTP_ATTEMPTS_EXCEEDED", "Too many wrong codes. Request a new one");
    }

    public static ApiException weakPassword() {
        return new ApiException(HttpStatus.BAD_REQUEST, "AUTH_PASSWORD_POLICY",
                "Password must be at least 8 characters with letters and digits");
    }

    public static ApiException wrongCurrentPassword() {
        return new ApiException(HttpStatus.BAD_REQUEST, "AUTH_PASSWORD_INCORRECT", "Current password is incorrect");
    }

    public static ApiException refreshInvalid() {
        return new ApiException(HttpStatus.UNAUTHORIZED, "AUTH_REFRESH_INVALID", "Session expired. Please log in again");
    }

    public static ApiException tokenReused() {
        return new ApiException(HttpStatus.UNAUTHORIZED, "AUTH_TOKEN_REUSED", "Session ended for security. Please log in again");
    }

    public static ApiException forbidden() {
        return new ApiException(HttpStatus.FORBIDDEN, "FORBIDDEN", "You do not have permission for this action");
    }

    public static ApiException badRequest(String message) {
        return new ApiException(HttpStatus.BAD_REQUEST, "VALIDATION_FAILED", message);
    }
}
