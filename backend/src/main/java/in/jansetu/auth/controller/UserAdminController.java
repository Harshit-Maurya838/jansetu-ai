package in.jansetu.auth.controller;

import in.jansetu.auth.dto.AdminUserDto;
import in.jansetu.auth.dto.CreateUserRequest;
import in.jansetu.auth.dto.UpdateStatusRequest;
import in.jansetu.auth.dto.UpdateUserRequest;
import in.jansetu.auth.entity.UserStatus;
import in.jansetu.auth.service.UserAdminService;
import in.jansetu.common.response.ApiResponse;
import in.jansetu.common.response.PageResponse;
import in.jansetu.security.JwtPrincipal;
import in.jansetu.security.Role;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * User management. SUPER_ADMIN: any user. NGO_ADMIN: NGO_WORKERs of own NGO. GOVT_SUPERVISOR: ANGANWADI_WORKERs of
 * centres in their jurisdiction. Scoping is enforced in UserAdminService (out of scope = 404).
 */
@Tag(name = "User management")
@RestController
@RequestMapping("/api/v1/admin/users")
@PreAuthorize("hasAnyRole('SUPER_ADMIN','NGO_ADMIN','GOVT_SUPERVISOR')")
@RequiredArgsConstructor
public class UserAdminController {
    private final UserAdminService service;

    @Operation(summary = "Create a user (PENDING_ACTIVATION; an activation OTP is sent to their mobile)")
    @PostMapping
    public ApiResponse<AdminUserDto> create(@AuthenticationPrincipal JwtPrincipal me, @Valid @RequestBody CreateUserRequest r) {
        return ApiResponse.ok(service.create(me, r));
    }

    @Operation(summary = "List users in my scope (q searches name, mobile, email)")
    @GetMapping
    public ApiResponse<PageResponse<AdminUserDto>> list(@AuthenticationPrincipal JwtPrincipal me,
                                                        @RequestParam(required = false) Role role,
                                                        @RequestParam(required = false) UserStatus status,
                                                        @RequestParam(required = false) UUID ngoId,
                                                        @RequestParam(required = false) UUID centreId,
                                                        @RequestParam(required = false) String q,
                                                        @ParameterObject @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ApiResponse.ok(service.list(me, role, status, ngoId, centreId, q, pageable));
    }

    @Operation(summary = "Get one user in my scope")
    @GetMapping("/{userId}")
    public ApiResponse<AdminUserDto> get(@AuthenticationPrincipal JwtPrincipal me, @PathVariable UUID userId) {
        return ApiResponse.ok(service.get(me, userId));
    }

    @Operation(summary = "Update details; role / NGO / jurisdiction changes are SUPER_ADMIN only")
    @PatchMapping("/{userId}")
    public ApiResponse<AdminUserDto> update(@AuthenticationPrincipal JwtPrincipal me, @PathVariable UUID userId,
                                            @Valid @RequestBody UpdateUserRequest r) {
        return ApiResponse.ok(service.update(me, userId, r));
    }

    @Operation(summary = "ACTIVATE, DISABLE (also ends all sessions) or UNLOCK a user")
    @PatchMapping("/{userId}/status")
    public ApiResponse<AdminUserDto> status(@AuthenticationPrincipal JwtPrincipal me, @PathVariable UUID userId,
                                            @Valid @RequestBody UpdateStatusRequest r) {
        return ApiResponse.ok(service.updateStatus(me, userId, r.action()));
    }
}
