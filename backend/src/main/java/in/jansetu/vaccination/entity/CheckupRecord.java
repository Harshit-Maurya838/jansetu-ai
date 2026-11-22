package in.jansetu.vaccination.entity;

import in.jansetu.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

/** Table `checkup_records`. Spec: docs/modules/04-vaccination-healthcare.md#data-model */
@Getter
@Setter
@Entity
@Table(name = "checkup_records")
public class CheckupRecord extends BaseEntity {

    @Column(nullable = false, length = 36)
    private UUID childId;  // FK -> children.id

    @Column(nullable = false)
    private LocalDate checkupOn;

    @Column(length = 120)
    private String doctorName;

    @Column(length = 160)
    private String facility;

    @Column(columnDefinition = "TEXT")
    private String findings;

    @Column(columnDefinition = "TEXT")
    private String prescriptions;

    @Column
    private LocalDate nextVisitOn;
}
