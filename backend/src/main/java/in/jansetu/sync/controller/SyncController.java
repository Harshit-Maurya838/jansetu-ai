package in.jansetu.sync.controller;

import in.jansetu.sync.service.SyncService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Offline Sync API.
 * TODO(team): implement the endpoints listed in docs/modules/16-offline-sync.md#api-endpoints,
 * each guarded with @PreAuthorize as per docs/rbac-matrix.md.
 */
@Tag(name = "Offline Sync")
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class SyncController {
    private final SyncService service;
}
