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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/** Table `welfare_records`. Spec: docs/modules/11-welfare-ngo-assistance.md#data-model */
@Getter
@Setter
@Entity
@Table(name = "welfare_records")
public class WelfareRecord extends BaseEntity {

    @Column(nullable = false, length = 36)
    private UUID beneficiaryId;  // FK -> elderly_beneficiaries.id

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(nullable = false, length = 30)
    private WelfareCategory category;

    @Column(nullable = false, length = 300)
    private String description;

    @Column(length = 60)
    private String quantity;  // e.g. 5 kg rice

    @Column(precision = 10, scale = 2)
    private BigDecimal amountInr;

    @Column(nullable = false)
    private LocalDate providedOn;

    @Column(nullable = false, length = 36)
    private UUID providedBy;  // FK -> users.id
}
