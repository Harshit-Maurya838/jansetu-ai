package in.jansetu.caregiver.entity;

import in.jansetu.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

/** Table `caregiver_assignments`. Spec: docs/modules/12-ngo-worker-caregiver.md#data-model */
@Getter
@Setter
@Entity
@Table(name = "caregiver_assignments")
public class CaregiverAssignment extends BaseEntity {

    @Column(nullable = false, length = 36)
    private UUID beneficiaryId;  // FK -> elderly_beneficiaries.id

    @Column(nullable = false, length = 36)
    private UUID workerId;  // FK -> users.id

    @Column(nullable = false)
    private LocalDate assignedFrom;

    @Column
    private LocalDate assignedTo;  // null = current
}
