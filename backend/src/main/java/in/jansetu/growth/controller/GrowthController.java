package in.jansetu.growth.controller;

import in.jansetu.growth.service.GrowthService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Growth & Nutrition API.
 * TODO(team): implement the endpoints listed in docs/modules/03-growth-nutrition.md#api-endpoints,
 * each guarded with @PreAuthorize as per docs/rbac-matrix.md.
 */
@Tag(name = "Growth & Nutrition")
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class GrowthController {
    private final GrowthService service;
}
