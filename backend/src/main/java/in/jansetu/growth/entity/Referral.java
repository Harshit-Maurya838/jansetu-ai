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

import java.time.LocalDate;
import java.util.UUID;

/** Table `referrals`. Spec: docs/modules/03-growth-nutrition.md#data-model */
@Getter
@Setter
@Entity
@Table(name = "referrals")
public class Referral extends BaseEntity {

    @Column(nullable = false, length = 36)
    private UUID childId;  // FK -> children.id

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(nullable = false, length = 30)
    private ReferralReason reason;

    @Column(nullable = false, length = 160)
    private String referredTo;  // e.g. NRC, PHC, CHC

    @Column(nullable = false)
    private LocalDate referredOn;

    @Column
    private LocalDate followUpOn;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(nullable = false, length = 30)
    private ReferralStatus status;

    @Column(length = 500)
    private String outcome;
}
