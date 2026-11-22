package in.jansetu.elderly.entity;

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
import java.time.LocalDate;
import java.util.UUID;

/** Table `elderly_beneficiaries`. Spec: docs/modules/08-elderly-registration.md#data-model */
@Getter
@Setter
@Entity
@Table(name = "elderly_beneficiaries", uniqueConstraints = {@UniqueConstraint(columnNames = {"beneficiary_code"})})
public class ElderlyBeneficiary extends BaseEntity {

    @Column(nullable = false, length = 24)
    private String beneficiaryCode;

    @Column(nullable = false, length = 120)
    private String fullName;

    @Column
    private LocalDate dateOfBirth;  // often unknown

    @Column(columnDefinition = "SMALLINT")
    private Integer approxAgeYears;  // used when DOB unknown

    @Column(length = 15)
    private String mobile;  // many beneficiaries have no phone

    @Column(nullable = false, length = 300)
    private String address;

    @Column(length = 80)
    private String village;

    @Column(nullable = false, length = 80)
    private String district;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(length = 30)
    private MaritalStatus maritalStatus;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(nullable = false, length = 30)
    private LivingArrangement livingArrangement;

    @Column(columnDefinition = "TEXT")
    private String medicalHistory;

    @Column(length = 300)
    private String disabilities;

    @Column(length = 300)
    private String welfareNeeds;

    @Column(nullable = false, length = 36)
    private UUID ngoId;  // FK -> ngos.id

    @Column(length = 36)
    private UUID assignedWorkerId;  // FK -> users.id

    @Column(nullable = false)
    private Instant consentGivenAt;

    @Column(nullable = false)
    private boolean locationConsent;  // required before SOS shares GPS

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(nullable = false, length = 30)
    private BeneficiaryStatus status;
}
