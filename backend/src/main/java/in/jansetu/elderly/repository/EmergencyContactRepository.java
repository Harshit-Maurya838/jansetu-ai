package in.jansetu.elderly.repository;

import in.jansetu.elderly.entity.EmergencyContact;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface EmergencyContactRepository extends JpaRepository<EmergencyContact, UUID> {
}
