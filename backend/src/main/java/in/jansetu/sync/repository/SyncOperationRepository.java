package in.jansetu.sync.repository;

import in.jansetu.sync.entity.SyncOperation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SyncOperationRepository extends JpaRepository<SyncOperation, UUID> {
}
