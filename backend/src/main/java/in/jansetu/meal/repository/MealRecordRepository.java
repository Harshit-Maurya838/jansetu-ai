package in.jansetu.meal.repository;

import in.jansetu.meal.entity.MealRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface MealRecordRepository extends JpaRepository<MealRecord, UUID> {
}
