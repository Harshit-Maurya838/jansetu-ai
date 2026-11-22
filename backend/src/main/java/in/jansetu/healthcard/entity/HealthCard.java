package in.jansetu.healthcard.entity;

import in.jansetu.common.entity.BaseEntity;
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

/** Table `health_cards`. Spec: docs/modules/06-qr-health-card.md#data-model */
@Getter
@Setter
@Entity
@Table(name = "health_cards", uniqueConstraints = {@UniqueConstraint(columnNames = {"token_id"})})
public class HealthCard extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(nullable = false, length = 30)
    private SubjectType subjectType;

    @Column(nullable = false, length = 36)
    private UUID subjectId;  // children.id or elderly_beneficiaries.id

    @Column(nullable = false, length = 64)
    private String tokenId;  // random id inside the signed QR token

    @Column(nullable = false)
    private Instant issuedAt;

    @Column
    private Instant revokedAt;  // set on reissue / loss
}
