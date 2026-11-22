package in.jansetu.sos.repository;

import in.jansetu.sos.entity.SosIncident;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SosIncidentRepository extends JpaRepository<SosIncident, UUID> {
}
