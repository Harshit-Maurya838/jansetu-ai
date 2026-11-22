package in.jansetu.caregiver.controller;

import in.jansetu.caregiver.service.CaregiverService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * NGO Workers & Caregivers API.
 * TODO(team): implement the endpoints listed in docs/modules/12-ngo-worker-caregiver.md#api-endpoints,
 * each guarded with @PreAuthorize as per docs/rbac-matrix.md.
 */
@Tag(name = "NGO Workers & Caregivers")
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class CaregiverController {
    private final CaregiverService service;
}
