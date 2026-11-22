package in.jansetu.growth.repository;

import in.jansetu.growth.entity.GrowthMeasurement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface GrowthMeasurementRepository extends JpaRepository<GrowthMeasurement, UUID> {
}
