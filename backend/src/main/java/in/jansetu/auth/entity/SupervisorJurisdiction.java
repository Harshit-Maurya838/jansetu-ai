package in.jansetu.auth.entity;

import in.jansetu.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

/** Table `supervisor_jurisdictions`: centres a GOVT_SUPERVISOR covers. Spec: docs/rbac-matrix.md#data-scoping */
@Getter
@Setter
@Entity
@Table(name = "supervisor_jurisdictions", uniqueConstraints = {@UniqueConstraint(columnNames = {"user_id", "centre_id"})})
public class SupervisorJurisdiction extends BaseEntity {

    @Column(nullable = false, length = 36)
    private UUID userId;  // FK -> users.id

    @Column(nullable = false, length = 36)
    private UUID centreId;  // FK -> anganwadi_centres.id
}
