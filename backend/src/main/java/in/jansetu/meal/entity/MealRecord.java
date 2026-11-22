package in.jansetu.meal.entity;

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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/** Table `meal_records`. Spec: docs/modules/07-midday-meal-quality.md#data-model */
@Getter
@Setter
@Entity
@Table(name = "meal_records", uniqueConstraints = {@UniqueConstraint(columnNames = {"centre_id", "meal_date", "meal_type"})})
public class MealRecord extends BaseEntity {

    @Column(nullable = false, length = 36)
    private UUID centreId;  // FK -> anganwadi_centres.id

    @Column(nullable = false)
    private LocalDate mealDate;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(nullable = false, length = 30)
    private MealType mealType;

    @Column(nullable = false, length = 300)
    private String menuItems;

    @Column(nullable = false)
    private Integer childrenServed;

    @Column(precision = 6, scale = 2)
    private BigDecimal quantityKg;

    @Column(precision = 4, scale = 1)
    private BigDecimal servingTempCelsius;

    @Column(nullable = false, columnDefinition = "SMALLINT")
    private Integer freshnessRating;  // 1-5

    @Column(nullable = false, columnDefinition = "SMALLINT")
    private Integer hygieneRating;  // 1-5

    @Column(columnDefinition = "SMALLINT")
    private Integer childFeedbackScore;  // 1-3 smiley scale

    @Column(length = 300)
    private String photoUrl;

    @Column(length = 36)
    private UUID supplierId;  // FK -> suppliers.id

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(length = 30)
    private MealQualityLabel aiQualityLabel;  // AI suggestion only

    @Column(precision = 4, scale = 3)
    private BigDecimal aiQualityScore;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(nullable = false, length = 30)
    private ReviewStatus reviewStatus;

    @Column(nullable = false, length = 36)
    private UUID recordedBy;  // FK -> users.id
}
