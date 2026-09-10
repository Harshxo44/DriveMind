package com.velora.backend.guardian;

import com.velora.backend.guardian.dto.CreateAlertRequest;
import com.velora.backend.guardian.dto.GuardianAlertResponse;
import com.velora.backend.guardian.dto.GuardianConfigResponse;
import com.velora.backend.guardian.dto.UpdateGuardianConfigRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/guardian")
public class GuardianController {

    private final GuardianService guardianService;

    public GuardianController(GuardianService guardianService) {
        this.guardianService = guardianService;
    }

    @GetMapping("/config/{vehicleId}")
    public ResponseEntity<GuardianConfigResponse> getConfig(@PathVariable UUID vehicleId) {
        return ResponseEntity.ok(guardianService.getOrCreateConfig(vehicleId));
    }

    @PutMapping("/config/{vehicleId}")
    public ResponseEntity<GuardianConfigResponse> updateConfig(
            @PathVariable UUID vehicleId,
            @RequestBody UpdateGuardianConfigRequest request) {
        return ResponseEntity.ok(guardianService.updateConfig(vehicleId, request));
    }

    @PostMapping("/alerts")
    public ResponseEntity<GuardianAlertResponse> createAlert(@Valid @RequestBody CreateAlertRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(guardianService.createAlert(request));
    }

    @GetMapping("/alerts/{vehicleId}")
    public ResponseEntity<List<GuardianAlertResponse>> getAlerts(@PathVariable UUID vehicleId) {
        return ResponseEntity.ok(guardianService.getAlerts(vehicleId));
    }

    @PostMapping("/alerts/{alertId}/ack")
    public ResponseEntity<GuardianAlertResponse> acknowledgeAlert(@PathVariable UUID alertId) {
        return ResponseEntity.ok(guardianService.acknowledgeAlert(alertId));
    }
}
