package in.jansetu.auth.controller;

import in.jansetu.auth.dto.ChangePasswordRequest;
import in.jansetu.auth.dto.DeviceTokenRequest;
import in.jansetu.auth.dto.UpdateProfileRequest;
import in.jansetu.auth.dto.UserDto;
import in.jansetu.auth.service.UserService;
import in.jansetu.common.response.ApiResponse;
import in.jansetu.security.JwtPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** The signed-in user's own account. Every role. */
@Tag(name = "My profile")
@RestController
@RequestMapping("/api/v1/users/me")
@PreAuthorize("isAuthenticated()")
@RequiredArgsConstructor
public class UserController {
    private final UserService service;

    @Operation(summary = "My profile, including permissions (module keys)")
    @GetMapping
    public ApiResponse<UserDto> me(@AuthenticationPrincipal JwtPrincipal me) {
        return ApiResponse.ok(service.me(me));
    }

    @Operation(summary = "Update my name, email or preferred language")
    @PatchMapping
    public ApiResponse<UserDto> update(@AuthenticationPrincipal JwtPrincipal me, @Valid @RequestBody UpdateProfileRequest r) {
        return ApiResponse.ok(service.updateMe(me, r));
    }

    @Operation(summary = "Change my password (other devices are logged out)")
    @PostMapping("/password")
    public ApiResponse<Void> changePassword(@AuthenticationPrincipal JwtPrincipal me, @Valid @RequestBody ChangePasswordRequest r) {
        service.changePassword(me, r);
        return ApiResponse.ok(null);
    }

    @Operation(summary = "Register this device's FCM push token")
    @PostMapping("/device-token")
    public ApiResponse<Void> deviceToken(@AuthenticationPrincipal JwtPrincipal me, @Valid @RequestBody DeviceTokenRequest r) {
        service.registerDeviceToken(me, r);
        return ApiResponse.ok(null);
    }
}
