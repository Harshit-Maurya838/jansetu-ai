package in.jansetu.caregiver.repository;

import in.jansetu.caregiver.entity.CaregiverAssignment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CaregiverAssignmentRepository extends JpaRepository<CaregiverAssignment, UUID> {
}
