package in.jansetu.admin.repository;

import in.jansetu.admin.entity.Ngo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface NgoRepository extends JpaRepository<Ngo, UUID> {
}
