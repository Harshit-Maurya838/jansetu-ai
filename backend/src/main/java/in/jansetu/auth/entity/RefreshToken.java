package in.jansetu.auth.entity;

import in.jansetu.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

/** Table `refresh_tokens`. Spec: docs/modules/01-authentication.md#data-model */
@Getter
@Setter
@Entity
@Table(name = "refresh_tokens", uniqueConstraints = {@UniqueConstraint(columnNames = {"token_hash"})})
public class RefreshToken extends BaseEntity {

    @Column(nullable = false, length = 36)
    private UUID userId;  // FK -> users.id

    @Column(nullable = false, length = 64)
    private String tokenHash;  // SHA-256 of the opaque token

    @Column(nullable = false, length = 64)
    private String deviceId;

    @Column(nullable = false)
    private Instant expiresAt;

    @Column
    private Instant revokedAt;
}
