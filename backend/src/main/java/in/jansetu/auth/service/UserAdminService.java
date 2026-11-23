package in.jansetu.auth.service;

import in.jansetu.admin.repository.AnganwadiCentreRepository;
import in.jansetu.admin.repository.NgoRepository;
import in.jansetu.auth.dto.AdminUserDto;
import in.jansetu.auth.dto.CreateUserRequest;
import in.jansetu.auth.dto.UpdateStatusRequest;
import in.jansetu.auth.dto.UpdateUserRequest;
import in.jansetu.auth.entity.OtpChannel;
import in.jansetu.auth.entity.OtpPurpose;
import in.jansetu.auth.entity.SupervisorJurisdiction;
import in.jansetu.auth.entity.User;
import in.jansetu.auth.entity.UserStatus;
import in.jansetu.auth.repository.SupervisorJurisdictionRepository;
import in.jansetu.auth.repository.UserRepository;
import in.jansetu.common.audit.AuditService;
import in.jansetu.common.exception.ApiException;
import in.jansetu.common.response.PageResponse;
import in.jansetu.security.JwtPrincipal;
import in.jansetu.security.Role;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

/**
 * User management for SUPER_ADMIN, NGO_ADMIN and GOVT_SUPERVISOR.
 * SA: anyone. NGA: views own NGO's staff, manages NGO_WORKERs of own NGO. GS: views and manages ANGANWADI_WORKERs of
 * centres in their jurisdiction. Out of view scope = 404; in view but not manageable = 403 (docs/rbac-matrix.md).
 */
@Service
@RequiredArgsConstructor
@Transactional
public class UserAdminService {
    private final UserRepository users;
    private final SupervisorJurisdictionRepository jurisdictions;
    private final AnganwadiCentreRepository centres;
    private final NgoRepository ngos;
    private final OtpService otp;
    private final RefreshTokenService refreshTokens;
    private final AuditService audit;

    public AdminUserDto create(JwtPrincipal me, CreateUserRequest r) {
        UUID centreId = r.centreId();
        UUID ngoId = r.ngoId();
        switch (me.role()) {
            case SUPER_ADMIN -> { }
            case NGO_ADMIN -> {
                if (r.role() != Role.NGO_WORKER) throw AuthErrors.forbidden();
                if (ngoId != null && !ngoId.equals(me.ngoId())) throw ApiException.notFound("NGO");
                ngoId = me.ngoId();
            }
            case GOVT_SUPERVISOR -> {
                if (r.role() != Role.ANGANWADI_WORKER) throw AuthErrors.forbidden();
                if (centreId == null || !myCentres(me).contains(centreId)) throw ApiException.notFound("Centre");
            }
            default -> throw AuthErrors.forbidden();
        }
        if (r.jurisdictionCentreIds() != null && me.role() != Role.SUPER_ADMIN) throw AuthErrors.forbidden();

        String mobile = Identifiers.mobile(r.mobile());
        if (users.existsByMobile(mobile)) throw ApiException.conflict("ALREADY_EXISTS", "This mobile number is already registered");
        String email = Identifiers.email(r.email());
        if (email != null && users.existsByEmail(email)) throw ApiException.conflict("ALREADY_EXISTS", "This email is already in use");

        var u = new User();
        u.setFullName(r.fullName().trim());
        u.setMobile(mobile);
        u.setEmail(email);
        u.setPreferredLanguage(r.preferredLanguage() == null ? "hi" : r.preferredLanguage());
        u.setStatus(UserStatus.PENDING_ACTIVATION);
        applyRoleAndScope(u, r.role(), centreId, ngoId);
        users.save(u);
        if (u.getRole() == Role.GOVT_SUPERVISOR) replaceJurisdictions(u.getId(), r.jurisdictionCentreIds());

        otp.issue(mobile, OtpChannel.SMS, OtpPurpose.ACTIVATION, u.getId());
        audit.record(me.userId(), AuthEvent.USER_CREATED.name(), "USER", u.getId(), me.deviceId(), "role=" + u.getRole());
        return toDto(u);
    }

    @Transactional(readOnly = true)
    public PageResponse<AdminUserDto> list(JwtPrincipal me, Role role, UserStatus status, UUID ngoId, UUID centreId,
                                           String q, Pageable pageable) {
        List<UUID> scopeCentres = me.role() == Role.GOVT_SUPERVISOR ? myCentres(me) : List.of();
        Specification<User> spec = (root, query, cb) -> {
            List<Predicate> p = new ArrayList<>();
            switch (me.role()) {
                case SUPER_ADMIN -> { }
                case NGO_ADMIN -> p.add(cb.equal(root.get("ngoId"), me.ngoId()));
                case GOVT_SUPERVISOR -> p.add(scopeCentres.isEmpty() ? cb.disjunction() : root.get("centreId").in(scopeCentres));
                default -> p.add(cb.disjunction());
            }
            if (role != null) p.add(cb.equal(root.get("role"), role));
            if (status != null) p.add(cb.equal(root.get("status"), status));
            if (ngoId != null) p.add(cb.equal(root.get("ngoId"), ngoId));
            if (centreId != null) p.add(cb.equal(root.get("centreId"), centreId));
            if (q != null && !q.isBlank()) {
                String like = "%" + q.trim().toLowerCase(Locale.ROOT) + "%";
                p.add(cb.or(cb.like(cb.lower(root.get("fullName")), like),
                        cb.like(root.get("mobile"), like),
                        cb.like(cb.lower(root.get("email")), like)));
            }
            return cb.and(p.toArray(Predicate[]::new));
        };
        return PageResponse.of(users.findAll(spec, pageable).map(this::toDto));
    }

    @Transactional(readOnly = true)
    public AdminUserDto get(JwtPrincipal me, UUID id) {
        User u = users.findById(id).orElseThrow(() -> ApiException.notFound("User"));
        if (!canView(me, u)) throw ApiException.notFound("User");
        return toDto(u);
    }

    public AdminUserDto update(JwtPrincipal me, UUID id, UpdateUserRequest r) {
        User u = loadManageable(me, id);
        Role newRole = r.role() != null ? r.role() : u.getRole();
        UUID centreId = r.centreId() != null ? r.centreId() : u.getCentreId();
        UUID ngoId = r.ngoId() != null ? r.ngoId() : u.getNgoId();
        if (me.role() != Role.SUPER_ADMIN) {
            if (newRole != u.getRole() || !Objects.equals(ngoId, u.getNgoId()) || r.jurisdictionCentreIds() != null) {
                throw AuthErrors.forbidden();
            }
            if (me.role() == Role.GOVT_SUPERVISOR && !myCentres(me).contains(centreId)) throw ApiException.notFound("Centre");
        }
        if (r.fullName() != null) u.setFullName(r.fullName().trim());
        if (r.preferredLanguage() != null) u.setPreferredLanguage(r.preferredLanguage());
        if (r.email() != null) {
            String email = Identifiers.email(r.email());
            if (!email.equals(u.getEmail()) && users.existsByEmail(email)) {
                throw ApiException.conflict("ALREADY_EXISTS", "This email is already in use");
            }
            u.setEmail(email);
        }
        Role oldRole = u.getRole();
        applyRoleAndScope(u, newRole, centreId, ngoId);
        if (newRole != Role.GOVT_SUPERVISOR) {
            if (oldRole == Role.GOVT_SUPERVISOR) jurisdictions.deleteByUserId(u.getId());
        } else if (r.jurisdictionCentreIds() != null) {
            replaceJurisdictions(u.getId(), r.jurisdictionCentreIds());
        }
        if (oldRole != newRole) {
            // Takes effect at the next token refresh (<= 15 min); refresh rebuilds the token from the DB.
            audit.record(me.userId(), AuthEvent.ROLE_CHANGED.name(), "USER", u.getId(), me.deviceId(), oldRole + "->" + newRole);
        }
        audit.record(me.userId(), AuthEvent.USER_UPDATED.name(), "USER", u.getId(), me.deviceId(), null);
        return toDto(u);
    }

    public AdminUserDto updateStatus(JwtPrincipal me, UUID id, UpdateStatusRequest.Action action) {
        User u = loadManageable(me, id);
        switch (action) {
            case ACTIVATE -> {
                clearLock(u);
                u.setStatus(u.getPasswordHash() == null ? UserStatus.PENDING_ACTIVATION : UserStatus.ACTIVE);
            }
            case DISABLE -> {
                u.setStatus(UserStatus.DISABLED);
                refreshTokens.revokeAll(u.getId());  // lost / stolen phone: sessions end at the next refresh
            }
            case UNLOCK -> clearLock(u);
        }
        audit.record(me.userId(), AuthEvent.USER_STATUS_CHANGED.name(), "USER", u.getId(), me.deviceId(), action.name());
        return toDto(u);
    }

    // ---- scoping ------------------------------------------------------------------------------

    private boolean canView(JwtPrincipal me, User target) {
        return switch (me.role()) {
            case SUPER_ADMIN -> true;
            case NGO_ADMIN -> me.ngoId() != null && me.ngoId().equals(target.getNgoId());
            case GOVT_SUPERVISOR -> target.getCentreId() != null && myCentres(me).contains(target.getCentreId());
            default -> false;
        };
    }

    private User loadManageable(JwtPrincipal me, UUID id) {
        User u = users.findById(id).orElseThrow(() -> ApiException.notFound("User"));
        if (!canView(me, u)) throw ApiException.notFound("User");
        if (u.getId().equals(me.userId())) {
            throw new ApiException(org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY, "BUSINESS_RULE",
                    "Use your profile to change your own account");
        }
        boolean manageable = switch (me.role()) {
            case SUPER_ADMIN -> true;
            case NGO_ADMIN -> u.getRole() == Role.NGO_WORKER;
            case GOVT_SUPERVISOR -> u.getRole() == Role.ANGANWADI_WORKER;
            default -> false;
        };
        if (!manageable) throw AuthErrors.forbidden();
        return u;
    }

    private List<UUID> myCentres(JwtPrincipal me) {
        return jurisdictions.findCentreIdsByUserId(me.userId());
    }

    /** AWW needs a centre; NGW / NGA need an NGO; other roles carry neither (GS scope = supervisor_jurisdictions). */
    private void applyRoleAndScope(User u, Role role, UUID centreId, UUID ngoId) {
        boolean needsCentre = role == Role.ANGANWADI_WORKER;
        boolean needsNgo = role == Role.NGO_WORKER || role == Role.NGO_ADMIN;
        if (needsCentre) {
            if (centreId == null) throw AuthErrors.badRequest("centreId is required for ANGANWADI_WORKER");
            if (!centres.existsById(centreId)) throw ApiException.notFound("Centre");
        }
        if (needsNgo) {
            if (ngoId == null) throw AuthErrors.badRequest("ngoId is required for " + role);
            if (!ngos.existsById(ngoId)) throw ApiException.notFound("NGO");
        }
        u.setRole(role);
        u.setCentreId(needsCentre ? centreId : null);
        u.setNgoId(needsNgo ? ngoId : null);
    }

    private void replaceJurisdictions(UUID userId, List<UUID> centreIds) {
        jurisdictions.deleteByUserId(userId);
        if (centreIds == null) return;
        for (UUID c : centreIds.stream().distinct().toList()) {
            if (!centres.existsById(c)) throw ApiException.notFound("Centre");
            var j = new SupervisorJurisdiction();
            j.setUserId(userId);
            j.setCentreId(c);
            jurisdictions.save(j);
        }
    }

    private static void clearLock(User u) {
        u.setLockedUntil(null);
        u.setFailedLoginAttempts(0);
        if (u.getStatus() == UserStatus.LOCKED) u.setStatus(UserStatus.ACTIVE);
    }

    private AdminUserDto toDto(User u) {
        return UserMapper.toAdminDto(u, u.getRole() == Role.GOVT_SUPERVISOR ? jurisdictions.findCentreIdsByUserId(u.getId()) : List.of());
    }
}
