package in.jansetu.vaccination.entity;

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

import java.time.LocalDate;
import java.util.UUID;

/** Table `vaccination_records`. Spec: docs/modules/04-vaccination-healthcare.md#data-model */
@Getter
@Setter
@Entity
@Table(name = "vaccination_records", uniqueConstraints = {@UniqueConstraint(columnNames = {"child_id", "schedule_item_id"})})
public class VaccinationRecord extends BaseEntity {

    @Column(nullable = false, length = 36)
    private UUID childId;  // FK -> children.id

    @Column(nullable = false, length = 36)
    private UUID scheduleItemId;  // FK -> vaccine_schedule.id

    @Column(nullable = false)
    private LocalDate dueOn;

    @Column
    private LocalDate givenOn;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(nullable = false, length = 30)
    private VaccinationStatus status;

    @Column(length = 40)
    private String batchNo;

    @Column(length = 160)
    private String facility;

    @Column(length = 500)
    private String notes;
}
