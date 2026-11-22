package in.jansetu.welfare.repository;

import in.jansetu.welfare.entity.WelfareRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface WelfareRecordRepository extends JpaRepository<WelfareRecord, UUID> {
}
