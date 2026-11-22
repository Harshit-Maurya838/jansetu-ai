package in.jansetu.meal.repository;

import in.jansetu.meal.entity.KitchenChecklist;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface KitchenChecklistRepository extends JpaRepository<KitchenChecklist, UUID> {
}
