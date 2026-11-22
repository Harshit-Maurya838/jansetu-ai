package in.jansetu.meal.entity;

import in.jansetu.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;


/** Table `suppliers`. Spec: docs/modules/07-midday-meal-quality.md#data-model */
@Getter
@Setter
@Entity
@Table(name = "suppliers")
public class Supplier extends BaseEntity {

    @Column(nullable = false, length = 160)
    private String name;

    @Column(length = 15)
    private String contactPhone;

    @Column(length = 20)
    private String fssaiLicenceNo;

    @Column(nullable = false, length = 80)
    private String district;

    @Column(nullable = false)
    private boolean active;
}
