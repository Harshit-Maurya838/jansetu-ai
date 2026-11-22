package in.jansetu.notification.entity;

import in.jansetu.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

/** Table `device_tokens`. Spec: docs/modules/14-notifications.md#data-model */
@Getter
@Setter
@Entity
@Table(name = "device_tokens", uniqueConstraints = {@UniqueConstraint(columnNames = {"fcm_token"})})
public class DeviceToken extends BaseEntity {

    @Column(nullable = false, length = 36)
    private UUID userId;  // FK -> users.id

    @Column(nullable = false, length = 255)
    private String fcmToken;

    @Column(nullable = false, length = 10)
    private String platform;  // android | ios

    @Column(nullable = false)
    private Instant lastSeenAt;
}
