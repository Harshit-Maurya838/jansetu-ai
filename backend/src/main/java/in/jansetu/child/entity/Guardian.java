package in.jansetu.child.entity;

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

import java.time.Instant;
import java.util.UUID;

/** Table `guardians`. Spec: docs/modules/02-child-management.md#data-model */
@Getter
@Setter
@Entity
@Table(name = "guardians")
public class Guardian extends BaseEntity {

    @Column(nullable = false, length = 120)
    private String fullName;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(nullable = false, length = 30)
    private GuardianRelation relation;

    @Column(length = 15)
    private String mobile;

    @Column(length = 15)
    private String alternateMobile;

    @Column(length = 300)
    private String address;

    @Column(length = 36)
    private UUID userId;  // FK -> users.id; set when the guardian has an app login

    @Column
    private Instant consentGivenAt;  // DPDP verifiable consent for the child's data

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(length = 30)
    private ConsentMethod consentMethod;
}
