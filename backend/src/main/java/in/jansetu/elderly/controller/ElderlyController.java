package in.jansetu.elderly.controller;

import in.jansetu.elderly.service.ElderlyService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Elderly Registration API.
 * TODO(team): implement the endpoints listed in docs/modules/08-elderly-registration.md#api-endpoints,
 * each guarded with @PreAuthorize as per docs/rbac-matrix.md.
 */
@Tag(name = "Elderly Registration")
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class ElderlyController {
    private final ElderlyService service;
}
