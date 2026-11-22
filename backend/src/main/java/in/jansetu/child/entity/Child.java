package in.jansetu.child.entity;

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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/** Table `children`. Spec: docs/modules/02-child-management.md#data-model */
@Getter
@Setter
@Entity
@Table(name = "children", uniqueConstraints = {@UniqueConstraint(columnNames = {"child_code"})})
public class Child extends BaseEntity {

    @Column(nullable = false, length = 24)
    private String childCode;  // human-readable unique ID printed on the card

    @Column(nullable = false, length = 120)
    private String fullName;

    @Column(nullable = false)
    private LocalDate dateOfBirth;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(nullable = false, length = 30)
    private Sex sex;

    @Column(nullable = false, length = 36)
    private UUID centreId;  // FK -> anganwadi_centres.id

    @Column(nullable = false, length = 36)
    private UUID guardianId;  // FK -> guardians.id

    @Column(precision = 4, scale = 2)
    private BigDecimal birthWeightKg;

    @Column(length = 5)
    private String bloodGroup;

    @Column(length = 500)
    private String allergies;

    @Column(columnDefinition = "TEXT")
    private String medicalHistory;

    @Column(length = 300)
    private String photoUrl;

    @Column(nullable = false)
    private LocalDate enrolledOn;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(nullable = false, length = 30)
    private ChildStatus status;
}
