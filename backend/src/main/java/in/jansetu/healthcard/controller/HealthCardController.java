package in.jansetu.healthcard.controller;

import in.jansetu.healthcard.service.HealthCardService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * QR Health Card API.
 * TODO(team): implement the endpoints listed in docs/modules/06-qr-health-card.md#api-endpoints,
 * each guarded with @PreAuthorize as per docs/rbac-matrix.md.
 */
@Tag(name = "QR Health Card")
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class HealthCardController {
    private final HealthCardService service;
}
