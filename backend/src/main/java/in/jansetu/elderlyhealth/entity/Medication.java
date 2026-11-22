package in.jansetu.elderlyhealth.entity;

import in.jansetu.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

/** Table `medications`. Spec: docs/modules/09-elderly-healthcare.md#data-model */
@Getter
@Setter
@Entity
@Table(name = "medications")
public class Medication extends BaseEntity {

    @Column(nullable = false, length = 36)
    private UUID beneficiaryId;  // FK -> elderly_beneficiaries.id

    @Column(nullable = false, length = 120)
    private String name;

    @Column(nullable = false, length = 60)
    private String dosage;  // e.g. 1 tablet 5 mg

    @Column(nullable = false, length = 60)
    private String timesOfDay;  // HH:mm list, e.g. 08:00,20:00

    @Column(nullable = false)
    private LocalDate startDate;

    @Column
    private LocalDate endDate;  // null = ongoing

    @Column(length = 120)
    private String prescribedBy;

    @Column(nullable = false)
    private boolean remindersEnabled;
}
