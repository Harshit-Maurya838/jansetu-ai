package in.jansetu.sos.entity;

import in.jansetu.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/** Table `sos_incidents`. Spec: docs/modules/10-emergency-sos.md#data-model */
@Getter
@Setter
@Entity
@Table(name = "sos_incidents")
public class SosIncident extends BaseEntity {

    @Column(nullable = false, length = 36)
    private UUID beneficiaryId;  // FK -> elderly_beneficiaries.id

    @Column(nullable = false)
    private Instant triggeredAt;  // device time

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(nullable = false, length = 30)
    private SosChannel channel;

    @Column(precision = 9, scale = 6)
    private BigDecimal latitude;  // only if locationConsent

    @Column(precision = 9, scale = 6)
    private BigDecimal longitude;

    @Column
    private Integer locationAccuracyMeters;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(nullable = false, length = 30)
    private SosStatus status;

    @Column(length = 36)
    private UUID acknowledgedBy;  // FK -> users.id

    @Column
    private Instant acknowledgedAt;

    @Column
    private Instant resolvedAt;

    @Column(length = 500)
    private String resolutionNotes;
}
