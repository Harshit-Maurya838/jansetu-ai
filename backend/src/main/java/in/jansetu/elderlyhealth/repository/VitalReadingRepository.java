package in.jansetu.elderlyhealth.repository;

import in.jansetu.elderlyhealth.entity.VitalReading;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface VitalReadingRepository extends JpaRepository<VitalReading, UUID> {
}
