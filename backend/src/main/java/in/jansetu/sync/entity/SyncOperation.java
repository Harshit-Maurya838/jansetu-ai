package in.jansetu.sync.entity;

import in.jansetu.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

/** Table `sync_operations`. Spec: docs/modules/16-offline-sync.md#data-model */
@Getter
@Setter
@Entity
@Table(name = "sync_operations", uniqueConstraints = {@UniqueConstraint(columnNames = {"op_id"})})
public class SyncOperation extends BaseEntity {

    @Column(nullable = false, length = 36)
    private UUID opId;  // client-generated; makes push idempotent

    @Column(nullable = false, length = 64)
    private String deviceId;

    @Column(nullable = false, length = 36)
    private UUID userId;  // FK -> users.id

    @Column(nullable = false, length = 40)
    private String entityType;

    @Column(nullable = false, length = 36)
    private UUID entityId;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(nullable = false, length = 30)
    private SyncOpType operation;

    @Column(nullable = false)
    private Instant clientUpdatedAt;

    @Column(nullable = false)
    private Instant receivedAt;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(nullable = false, length = 30)
    private SyncResult result;

    @Column(length = 500)
    private String detail;
}
