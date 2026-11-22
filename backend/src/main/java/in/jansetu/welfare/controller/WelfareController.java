package in.jansetu.welfare.controller;

import in.jansetu.welfare.service.WelfareService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Welfare & NGO Assistance API.
 * TODO(team): implement the endpoints listed in docs/modules/11-welfare-ngo-assistance.md#api-endpoints,
 * each guarded with @PreAuthorize as per docs/rbac-matrix.md.
 */
@Tag(name = "Welfare & NGO Assistance")
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class WelfareController {
    private final WelfareService service;
}
