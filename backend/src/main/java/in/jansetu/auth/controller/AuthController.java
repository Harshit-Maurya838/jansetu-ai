package in.jansetu.auth.controller;

import in.jansetu.auth.dto.ActivateRequest;
import in.jansetu.auth.dto.AuthResponse;
import in.jansetu.auth.dto.ForgotPasswordRequest;
import in.jansetu.auth.dto.LoginRequest;
import in.jansetu.auth.dto.MobileRequest;
import in.jansetu.auth.dto.OtpLoginRequest;
import in.jansetu.auth.dto.OtpSentResponse;
import in.jansetu.auth.dto.RefreshRequest;
import in.jansetu.auth.dto.RegisterVerifyRequest;
import in.jansetu.auth.dto.ResetPasswordRequest;
import in.jansetu.auth.dto.SessionDto;
import in.jansetu.auth.service.AuthService;
import in.jansetu.common.response.ApiResponse;
import in.jansetu.security.JwtPrincipal;
import in.jansetu.security.ratelimit.AuthRateLimits;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/** Authentication API. Spec: docs/modules/01-authentication.md#api-endpoints */
@Tag(name = "Authentication")
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService service;
    private final AuthRateLimits rateLimits;

    @Operation(summary = "Parent self-signup, step 1: send an OTP to the mobile number (generic response)")
    @SecurityRequirements
    @PostMapping("/register/request-otp")
    public ApiResponse<OtpSentResponse> registerRequestOtp(@Valid @RequestBody MobileRequest r) {
        rateLimits.checkTarget(r.mobile());
        service.requestRegistrationOtp(r.mobile());
        return ApiResponse.ok(OtpSentResponse.GENERIC);
    }

    @Operation(summary = "Parent self-signup, step 2: verify OTP, set name and password, start a session")
    @SecurityRequirements
    @PostMapping("/register/verify")
    public ApiResponse<AuthResponse> registerVerify(@Valid @RequestBody RegisterVerifyRequest r) {
        rateLimits.checkTarget(r.mobile());
        return ApiResponse.ok(service.verifyRegistration(r));
    }

    @Operation(summary = "Password login with mobile number or email")
    @SecurityRequirements
    @PostMapping("/login")
    public ApiResponse<AuthResponse> login(@Valid @RequestBody LoginRequest r) {
        rateLimits.checkTarget(r.identifier());
        return ApiResponse.ok(service.login(r));
    }

    @Operation(summary = "OTP login, step 1: send an OTP to a registered mobile (generic response)")
    @SecurityRequirements
    @PostMapping("/login/otp/request")
    public ApiResponse<OtpSentResponse> loginOtpRequest(@Valid @RequestBody MobileRequest r) {
        rateLimits.checkTarget(r.mobile());
        service.requestLoginOtp(r.mobile());
        return ApiResponse.ok(OtpSentResponse.GENERIC);
    }

    @Operation(summary = "OTP login, step 2: verify OTP and start a session")
    @SecurityRequirements
    @PostMapping("/login/otp/verify")
    public ApiResponse<AuthResponse> loginOtpVerify(@Valid @RequestBody OtpLoginRequest r) {
        rateLimits.checkTarget(r.mobile());
        return ApiResponse.ok(service.verifyLoginOtp(r));
    }

    @Operation(summary = "Admin-created user: send a new activation OTP (generic response)")
    @SecurityRequirements
    @PostMapping("/activate/request-otp")
    public ApiResponse<OtpSentResponse> activateRequestOtp(@Valid @RequestBody MobileRequest r) {
        rateLimits.checkTarget(r.mobile());
        service.requestActivationOtp(r.mobile());
        return ApiResponse.ok(OtpSentResponse.GENERIC);
    }

    @Operation(summary = "Admin-created user: verify activation OTP, set own password, start a session")
    @SecurityRequirements
    @PostMapping("/activate")
    public ApiResponse<AuthResponse> activate(@Valid @RequestBody ActivateRequest r) {
        rateLimits.checkTarget(r.mobile());
        return ApiResponse.ok(service.activate(r));
    }

    @Operation(summary = "Forgot password: send a reset OTP to the mobile or email (generic response)")
    @SecurityRequirements
    @PostMapping("/password/forgot")
    public ApiResponse<OtpSentResponse> forgotPassword(@Valid @RequestBody ForgotPasswordRequest r) {
        rateLimits.checkTarget(r.identifier());
        service.forgotPassword(r.identifier());
        return ApiResponse.ok(OtpSentResponse.GENERIC);
    }

    @Operation(summary = "Reset password with the OTP; ends every session of the user")
    @SecurityRequirements
    @PostMapping("/password/reset")
    public ApiResponse<Void> resetPassword(@Valid @RequestBody ResetPasswordRequest r) {
        rateLimits.checkTarget(r.identifier());
        service.resetPassword(r);
        return ApiResponse.ok(null);
    }

    @Operation(summary = "Rotate the refresh token: returns a new access + refresh token pair")
    @SecurityRequirements
    @PostMapping("/token/refresh")
    public ApiResponse<AuthResponse> refresh(@Valid @RequestBody RefreshRequest r) {
        return ApiResponse.ok(service.refresh(r.refreshToken(), r.deviceId()));
    }

    @Operation(summary = "Log out this device")
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/logout")
    public ApiResponse<Void> logout(@AuthenticationPrincipal JwtPrincipal me) {
        service.logout(me);
        return ApiResponse.ok(null);
    }

    @Operation(summary = "Log out every device")
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/logout-all")
    public ApiResponse<Void> logoutAll(@AuthenticationPrincipal JwtPrincipal me) {
        service.logoutAll(me);
        return ApiResponse.ok(null);
    }

    @Operation(summary = "List my active device sessions")
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/sessions")
    public ApiResponse<List<SessionDto>> sessions(@AuthenticationPrincipal JwtPrincipal me) {
        return ApiResponse.ok(service.sessions(me));
    }

    @Operation(summary = "End one of my device sessions")
    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/sessions/{sessionId}")
    public ApiResponse<Void> revokeSession(@AuthenticationPrincipal JwtPrincipal me, @PathVariable UUID sessionId) {
        service.revokeSession(me, sessionId);
        return ApiResponse.ok(null);
    }
}
