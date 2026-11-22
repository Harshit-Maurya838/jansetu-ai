package in.jansetu.admin.entity;

import in.jansetu.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;


/** Table `ngos`. Spec: docs/modules/15-admin-dashboard-reports.md#data-model */
@Getter
@Setter
@Entity
@Table(name = "ngos")
public class Ngo extends BaseEntity {

    @Column(nullable = false, length = 160)
    private String name;

    @Column(length = 60)
    private String registrationNo;  // TODO(team): confirm which registration id partner NGOs share

    @Column(nullable = false, length = 80)
    private String district;

    @Column(nullable = false, length = 80)
    private String state;

    @Column(length = 15)
    private String contactPhone;
}
