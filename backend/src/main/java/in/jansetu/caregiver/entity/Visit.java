package in.jansetu.caregiver.entity;

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

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/** Table `visits`. Spec: docs/modules/12-ngo-worker-caregiver.md#data-model */
@Getter
@Setter
@Entity
@Table(name = "visits")
public class Visit extends BaseEntity {

    @Column(nullable = false, length = 36)
    private UUID workerId;  // FK -> users.id

    @Column(nullable = false, length = 36)
    private UUID beneficiaryId;  // FK -> elderly_beneficiaries.id

    @Column(nullable = false)
    private LocalDate scheduledFor;

    @Column
    private Instant visitedAt;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(nullable = false, length = 30)
    private VisitStatus status;

    @Column(nullable = false, length = 200)
    private String purpose;

    @Column(length = 1000)
    private String notes;

    @Column
    private LocalDate followUpOn;
}
