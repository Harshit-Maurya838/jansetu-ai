package in.jansetu.child.controller;

import in.jansetu.child.service.ChildService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Child Management API.
 * TODO(team): implement the endpoints listed in docs/modules/02-child-management.md#api-endpoints,
 * each guarded with @PreAuthorize as per docs/rbac-matrix.md.
 */
@Tag(name = "Child Management")
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class ChildController {
    private final ChildService service;
}
