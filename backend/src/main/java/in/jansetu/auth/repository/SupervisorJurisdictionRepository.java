package in.jansetu.auth.repository;

import in.jansetu.auth.entity.SupervisorJurisdiction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SupervisorJurisdictionRepository extends JpaRepository<SupervisorJurisdiction, UUID> {
}
