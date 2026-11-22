package in.jansetu.meal.entity;

import in.jansetu.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

/** Table `kitchen_checklists`. Spec: docs/modules/07-midday-meal-quality.md#data-model */
@Getter
@Setter
@Entity
@Table(name = "kitchen_checklists", uniqueConstraints = {@UniqueConstraint(columnNames = {"meal_record_id"})})
public class KitchenChecklist extends BaseEntity {

    @Column(nullable = false, length = 36)
    private UUID mealRecordId;  // FK -> meal_records.id

    @Column(nullable = false)
    private boolean handwashAvailable;

    @Column(nullable = false)
    private boolean utensilsClean;

    @Column(nullable = false)
    private boolean drinkingWaterSafe;

    @Column(nullable = false)
    private boolean foodCovered;

    @Column(nullable = false)
    private boolean cookHeadCovered;

    @Column(nullable = false)
    private boolean storageClean;

    @Column(nullable = false)
    private boolean pestFree;

    @Column(length = 300)
    private String remarks;
}
