package in.jansetu.analytics.entity;

import in.jansetu.common.entity.BaseEntity;
import in.jansetu.healthcard.entity.SubjectType;
import in.jansetu.meal.entity.ReviewStatus;
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

/** Table `risk_alerts`. Spec: docs/modules/13-ai-analytics.md#data-model */
@Getter
@Setter
@Entity
@Table(name = "risk_alerts")
public class RiskAlert extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(nullable = false, length = 30)
    private SubjectType subjectType;

    @Column(nullable = false, length = 36)
    private UUID subjectId;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(nullable = false, length = 30)
    private RiskAlertType alertType;

    @Column(nullable = false, precision = 4, scale = 3)
    private BigDecimal riskScore;  // 0-1

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(nullable = false, length = 30)
    private RiskLevel riskLevel;

    @Column(nullable = false, length = 60)
    private String modelName;

    @Column(nullable = false, length = 20)
    private String modelVersion;

    @Column(length = 500)
    private String explanation;  // top contributing features, plain language

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(nullable = false, length = 30)
    private ReviewStatus reviewStatus;

    @Column(length = 36)
    private UUID reviewedBy;  // FK -> users.id

    @Column
    private Instant reviewedAt;

    @Column(length = 500)
    private String reviewNote;
}
