package in.jansetu.auth.entity;

import in.jansetu.common.entity.BaseEntity;
import in.jansetu.security.Role;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

/** Table `users`. Spec: docs/modules/01-authentication.md#data-model */
@Getter
@Setter
@Entity
@Table(name = "users", uniqueConstraints = {@UniqueConstraint(columnNames = {"mobile"}), @UniqueConstraint(columnNames = {"email"})})
public class User extends BaseEntity {

    @Column(nullable = false, length = 120)
    private String fullName;

    @Column(nullable = false, length = 15)
    private String mobile;  // E.164, e.g. +919876543210

    @Column(length = 160)
    private String email;

    @Column(length = 100)
    private String passwordHash;  // BCrypt; null for OTP-only users

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(nullable = false, length = 30)
    private Role role;

    @Column(nullable = false, length = 5)
    private String preferredLanguage;  // en | hi

    @Column(length = 36)
    private UUID centreId;  // FK -> anganwadi_centres.id; Anganwadi workers/supervisors

    @Column(length = 36)
    private UUID ngoId;  // FK -> ngos.id; NGO staff

    @Column(nullable = false)
    private boolean active;

    @Column
    private Instant lastLoginAt;
}
