package in.jansetu.sos.entity;

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

/** Table `sos_events`. Spec: docs/modules/10-emergency-sos.md#data-model */
@Getter
@Setter
@Entity
@Table(name = "sos_events")
public class SosEvent extends BaseEntity {

    @Column(nullable = false, length = 36)
    private UUID incidentId;  // FK -> sos_incidents.id

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(nullable = false, length = 30)
    private SosEventType eventType;

    @Column(length = 36)
    private UUID actorUserId;  // FK -> users.id

    @Column(length = 500)
    private String note;

    @Column(nullable = false)
    private Instant occurredAt;
}
