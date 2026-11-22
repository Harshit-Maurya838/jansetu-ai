package in.jansetu.elderlyhealth.repository;

import in.jansetu.elderlyhealth.entity.Medication;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface MedicationRepository extends JpaRepository<Medication, UUID> {
}
