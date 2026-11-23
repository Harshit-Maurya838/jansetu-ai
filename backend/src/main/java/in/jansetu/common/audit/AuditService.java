package in.jansetu.common.audit;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuditService {
    private final AuditLogRepository repository;

    // TODO(team): call from services for every read/write of health data, QR scans, SOS actions and exports.
    public void record(UUID actor, String action, String entityType, UUID entityId, String details) {
        record(actor, action, entityType, entityId, null, details);
    }

    /** Same as above, with the device; the client IP is taken from the current request when there is one. */
    public void record(UUID actor, String action, String entityType, UUID entityId, String deviceId, String details) {
        var log = new AuditLog();
        log.setActorUserId(actor);
        log.setAction(action);
        log.setEntityType(entityType);
        log.setEntityId(entityId);
        log.setDeviceId(deviceId);
        log.setIp(clientIp());
        log.setDetails(details);
        log.setOccurredAt(Instant.now());
        repository.save(log);
    }

    // shortcut: remote address only; behind a load balancer, configure server.forward-headers-strategy so this is the real client.
    private static String clientIp() {
        return RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes a
                ? a.getRequest().getRemoteAddr() : null;
    }
}
