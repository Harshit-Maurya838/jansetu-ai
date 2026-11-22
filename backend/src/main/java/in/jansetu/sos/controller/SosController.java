package in.jansetu.sos.controller;

import in.jansetu.sos.service.SosService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Emergency SOS API.
 * TODO(team): implement the endpoints listed in docs/modules/10-emergency-sos.md#api-endpoints,
 * each guarded with @PreAuthorize as per docs/rbac-matrix.md.
 */
@Tag(name = "Emergency SOS")
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class SosController {
    private final SosService service;
}
