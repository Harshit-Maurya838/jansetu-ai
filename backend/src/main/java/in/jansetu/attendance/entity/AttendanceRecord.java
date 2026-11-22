package in.jansetu.attendance.entity;

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

/** Table `attendance_records`. Spec: docs/modules/05-attendance.md#data-model */
@Getter
@Setter
@Entity
@Table(name = "attendance_records", uniqueConstraints = {@UniqueConstraint(columnNames = {"child_id", "attendance_date"})})
public class AttendanceRecord extends BaseEntity {

    @Column(nullable = false, length = 36)
    private UUID childId;  // FK -> children.id

    @Column(nullable = false, length = 36)
    private UUID centreId;  // FK -> anganwadi_centres.id

    @Column(nullable = false)
    private LocalDate attendanceDate;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(nullable = false, length = 30)
    private AttendanceStatus status;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(length = 30)
    private AbsenceReason absenceReason;

    @Column(nullable = false, length = 36)
    private UUID markedBy;  // FK -> users.id
}
