package in.jansetu.vaccination.repository;

import in.jansetu.vaccination.entity.VaccinationRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface VaccinationRecordRepository extends JpaRepository<VaccinationRecord, UUID> {
}
