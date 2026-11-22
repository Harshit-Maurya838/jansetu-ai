package in.jansetu.welfare.repository;

import in.jansetu.welfare.entity.SchemeApplication;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SchemeApplicationRepository extends JpaRepository<SchemeApplication, UUID> {
}
