package in.jansetu.vaccination.controller;

import in.jansetu.vaccination.service.VaccinationService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Vaccination & Healthcare API.
 * TODO(team): implement the endpoints listed in docs/modules/04-vaccination-healthcare.md#api-endpoints,
 * each guarded with @PreAuthorize as per docs/rbac-matrix.md.
 */
@Tag(name = "Vaccination & Healthcare")
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class VaccinationController {
    private final VaccinationService service;
}
