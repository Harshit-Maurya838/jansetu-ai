package in.jansetu.elderlyhealth.entity;

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

/** Table `vital_readings`. Spec: docs/modules/09-elderly-healthcare.md#data-model */
@Getter
@Setter
@Entity
@Table(name = "vital_readings")
public class VitalReading extends BaseEntity {

    @Column(nullable = false, length = 36)
    private UUID beneficiaryId;  // FK -> elderly_beneficiaries.id

    @Column(nullable = false)
    private Instant recordedAt;

    @Column(columnDefinition = "SMALLINT")
    private Integer systolic;  // mmHg

    @Column(columnDefinition = "SMALLINT")
    private Integer diastolic;  // mmHg

    @Column(columnDefinition = "SMALLINT")
    private Integer pulse;  // bpm

    @Column(columnDefinition = "SMALLINT")
    private Integer bloodGlucoseMgDl;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(length = 30)
    private GlucoseContext glucoseContext;

    @Column(precision = 5, scale = 2)
    private BigDecimal weightKg;

    @Column(nullable = false, length = 36)
    private UUID recordedBy;  // FK -> users.id
}
