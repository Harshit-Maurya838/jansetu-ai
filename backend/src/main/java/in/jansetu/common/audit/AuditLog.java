package in.jansetu.common.audit;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

/** Append-only record of who read/changed sensitive data. See docs/privacy-and-security.md#audit-logging. */
@Getter
@Setter
@Entity
@Table(name = "audit_logs")
public class AuditLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 36)
    private UUID actorUserId;

    @Column(nullable = false, length = 40)
    private String action;          // e.g. CHILD_VIEW, QR_SCAN, SOS_ACK, REPORT_EXPORT

    @Column(nullable = false, length = 40)
    private String entityType;

    @Column(length = 36)
    private UUID entityId;

    @Column(length = 64)
    private String deviceId;

    @Column(length = 500)
    private String details;         // never put health values here

    @Column(nullable = false)
    private Instant occurredAt;
}
