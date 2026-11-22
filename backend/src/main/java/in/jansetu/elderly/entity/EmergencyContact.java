package in.jansetu.elderly.entity;

import in.jansetu.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

/** Table `emergency_contacts`. Spec: docs/modules/08-elderly-registration.md#data-model */
@Getter
@Setter
@Entity
@Table(name = "emergency_contacts")
public class EmergencyContact extends BaseEntity {

    @Column(nullable = false, length = 36)
    private UUID beneficiaryId;  // FK -> elderly_beneficiaries.id

    @Column(nullable = false, length = 120)
    private String name;

    @Column(nullable = false, length = 40)
    private String relation;

    @Column(nullable = false, length = 15)
    private String mobile;

    @Column(nullable = false, columnDefinition = "SMALLINT")
    private Integer priority;  // 1 = call first

    @Column(nullable = false)
    private boolean notifyOnSos;
}
