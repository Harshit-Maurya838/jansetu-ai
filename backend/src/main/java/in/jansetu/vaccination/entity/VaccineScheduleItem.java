package in.jansetu.vaccination.entity;

import in.jansetu.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;


/** Table `vaccine_schedule`. Spec: docs/modules/04-vaccination-healthcare.md#data-model */
@Getter
@Setter
@Entity
@Table(name = "vaccine_schedule", uniqueConstraints = {@UniqueConstraint(columnNames = {"vaccine_code", "dose_label"})})
public class VaccineScheduleItem extends BaseEntity {

    @Column(nullable = false, length = 20)
    private String vaccineCode;

    @Column(nullable = false, length = 80)
    private String name;

    @Column(nullable = false, length = 30)
    private String doseLabel;

    @Column(nullable = false)
    private Integer dueAgeDays;

    @Column
    private Integer maxAgeDays;  // last age at which the dose may still be given

    @Column(nullable = false)
    private Integer sequenceNo;

    @Column(nullable = false)
    private boolean active;
}
