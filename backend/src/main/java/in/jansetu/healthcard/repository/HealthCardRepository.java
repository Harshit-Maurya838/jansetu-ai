package in.jansetu.healthcard.repository;

import in.jansetu.healthcard.entity.HealthCard;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface HealthCardRepository extends JpaRepository<HealthCard, UUID> {
}
