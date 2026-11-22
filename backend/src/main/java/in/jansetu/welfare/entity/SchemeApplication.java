package in.jansetu.welfare.entity;

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

import java.time.LocalDate;
import java.util.UUID;

/** Table `scheme_applications`. Spec: docs/modules/11-welfare-ngo-assistance.md#data-model */
@Getter
@Setter
@Entity
@Table(name = "scheme_applications")
public class SchemeApplication extends BaseEntity {

    @Column(nullable = false, length = 36)
    private UUID beneficiaryId;  // FK -> elderly_beneficiaries.id

    @Column(nullable = false, length = 160)
    private String schemeName;

    @Column(length = 60)
    private String applicationRef;  // reference from the govt portal

    @Column
    private LocalDate appliedOn;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(nullable = false, length = 30)
    private ApplicationStatus status;

    @Column
    private LocalDate lastStatusOn;

    @Column(length = 500)
    private String notes;
}
