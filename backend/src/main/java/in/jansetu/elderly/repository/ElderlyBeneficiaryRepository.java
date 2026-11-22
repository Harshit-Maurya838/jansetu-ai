package in.jansetu.elderly.repository;

import in.jansetu.elderly.entity.ElderlyBeneficiary;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ElderlyBeneficiaryRepository extends JpaRepository<ElderlyBeneficiary, UUID> {
}
