package in.jansetu.sos.repository;

import in.jansetu.sos.entity.SosEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SosEventRepository extends JpaRepository<SosEvent, UUID> {
}
