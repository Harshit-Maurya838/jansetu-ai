package in.jansetu.admin.entity;

import in.jansetu.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/** Table `anganwadi_centres`. Spec: docs/modules/15-admin-dashboard-reports.md#data-model */
@Getter
@Setter
@Entity
@Table(name = "anganwadi_centres", uniqueConstraints = {@UniqueConstraint(columnNames = {"code"})})
public class AnganwadiCentre extends BaseEntity {

    @Column(nullable = false, length = 30)
    private String code;  // Govt AWC code

    @Column(nullable = false, length = 120)
    private String name;

    @Column(length = 80)
    private String village;

    @Column(length = 80)
    private String block;

    @Column(nullable = false, length = 80)
    private String district;

    @Column(nullable = false, length = 80)
    private String state;

    @Column(precision = 9, scale = 6)
    private BigDecimal latitude;

    @Column(precision = 9, scale = 6)
    private BigDecimal longitude;
}
