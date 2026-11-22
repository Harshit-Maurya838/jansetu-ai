package in.jansetu.admin.repository;

import in.jansetu.admin.entity.AnganwadiCentre;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AnganwadiCentreRepository extends JpaRepository<AnganwadiCentre, UUID> {
}
