package in.jansetu.analytics.repository;

import in.jansetu.analytics.entity.RiskAlert;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface RiskAlertRepository extends JpaRepository<RiskAlert, UUID> {
}
