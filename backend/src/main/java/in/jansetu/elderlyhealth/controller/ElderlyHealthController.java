package in.jansetu.elderlyhealth.controller;

import in.jansetu.elderlyhealth.service.ElderlyHealthService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Elderly Healthcare API.
 * TODO(team): implement the endpoints listed in docs/modules/09-elderly-healthcare.md#api-endpoints,
 * each guarded with @PreAuthorize as per docs/rbac-matrix.md.
 */
@Tag(name = "Elderly Healthcare")
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class ElderlyHealthController {
    private final ElderlyHealthService service;
}
