package in.jansetu.growth.entity;

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
import java.time.LocalDate;
import java.util.UUID;

/** Table `growth_measurements`. Spec: docs/modules/03-growth-nutrition.md#data-model */
@Getter
@Setter
@Entity
@Table(name = "growth_measurements")
public class GrowthMeasurement extends BaseEntity {

    @Column(nullable = false, length = 36)
    private UUID childId;  // FK -> children.id

    @Column(nullable = false)
    private LocalDate measuredOn;

    @Column(nullable = false)
    private Integer ageDays;  // computed from DOB at measuredOn

    @Column(precision = 5, scale = 2)
    private BigDecimal weightKg;

    @Column(precision = 5, scale = 1)
    private BigDecimal heightCm;  // length if < 2 years

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(length = 30)
    private MeasurementPosition measurementPosition;

    @Column(precision = 4, scale = 1)
    private BigDecimal muacCm;  // mid-upper arm circumference, 6-59 months

    @Column(nullable = false)
    private boolean oedema;  // bilateral pitting oedema => SAM regardless of z-score

    @Column(precision = 5, scale = 2)
    private BigDecimal waz;  // weight-for-age z

    @Column(precision = 5, scale = 2)
    private BigDecimal haz;  // height-for-age z

    @Column(precision = 5, scale = 2)
    private BigDecimal whz;  // weight-for-height z

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(nullable = false, length = 30)
    private NutritionStatus nutritionStatus;

    @Column(nullable = false, length = 36)
    private UUID measuredBy;  // FK -> users.id
}
