package in.jansetu.caregiver.repository;

import in.jansetu.caregiver.entity.Visit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface VisitRepository extends JpaRepository<Visit, UUID> {
}
