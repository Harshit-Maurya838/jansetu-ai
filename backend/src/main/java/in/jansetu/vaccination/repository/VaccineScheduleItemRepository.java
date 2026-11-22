package in.jansetu.vaccination.repository;

import in.jansetu.vaccination.entity.VaccineScheduleItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface VaccineScheduleItemRepository extends JpaRepository<VaccineScheduleItem, UUID> {
}
