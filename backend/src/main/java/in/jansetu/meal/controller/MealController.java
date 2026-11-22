package in.jansetu.meal.controller;

import in.jansetu.meal.service.MealService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Midday Meal Quality API.
 * TODO(team): implement the endpoints listed in docs/modules/07-midday-meal-quality.md#api-endpoints,
 * each guarded with @PreAuthorize as per docs/rbac-matrix.md.
 */
@Tag(name = "Midday Meal Quality")
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class MealController {
    private final MealService service;
}
