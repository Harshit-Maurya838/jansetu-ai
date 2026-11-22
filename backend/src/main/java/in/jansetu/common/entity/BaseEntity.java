package in.jansetu.common.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Version;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.UUID;

/**
 * Columns shared by every synced table (see docs/architecture.md#offline-sync-design).
 * The id is generated on the device for offline-created rows, so the server must accept it as-is.
 */
@Getter
@Setter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity {

    @Id
    @Column(length = 36)
    private UUID id;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    // TODO(team): the sync service must set this from the client's change timestamp for last-write-wins,
    // instead of letting auditing overwrite it with server time.
    @LastModifiedDate
    @Column(nullable = false)
    private Instant updatedAt;

    @CreatedBy
    @Column(length = 36, updatable = false)
    private UUID createdBy;

    @LastModifiedBy
    @Column(length = 36)
    private UUID updatedBy;

    @Version
    private Long version;

    /** Soft delete: rows are never physically removed so deletes can sync and be audited. */
    @Column(nullable = false)
    private boolean deleted;

    @PrePersist
    void ensureId() {
        if (id == null) id = UUID.randomUUID();
    }
}
