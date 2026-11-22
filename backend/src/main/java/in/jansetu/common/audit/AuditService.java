package in.jansetu.common.audit;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuditService {
    private final AuditLogRepository repository;

    // TODO(team): call from services for every read/write of health data, QR scans, SOS actions and exports.
    public void record(UUID actor, String action, String entityType, UUID entityId, String details) {
        var log = new AuditLog();
        log.setActorUserId(actor);
        log.setAction(action);
        log.setEntityType(entityType);
        log.setEntityId(entityId);
        log.setDetails(details);
        log.setOccurredAt(Instant.now());
        repository.save(log);
    }
}
