package in.jansetu.vaccination.repository;

import in.jansetu.vaccination.entity.CheckupRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CheckupRecordRepository extends JpaRepository<CheckupRecord, UUID> {
}
