package com.velora.backend.diagnostics;

import com.velora.backend.diagnostics.dto.AiAssistantRequest;
import com.velora.backend.diagnostics.dto.AiAssistantResponse;
import com.velora.backend.diagnostics.dto.VehicleHealthSummaryResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/diagnostics")
public class DiagnosticsController {

    private final DiagnosticsService diagnosticsService;

    public DiagnosticsController(DiagnosticsService diagnosticsService) {
        this.diagnosticsService = diagnosticsService;
    }

    @GetMapping("/dtc/{code}")
    public ResponseEntity<DiagnosticTroubleCode> lookupDtc(@PathVariable String code) {
        return diagnosticsService.lookupDtc(code)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/health/{vehicleId}")
    public ResponseEntity<VehicleHealthSummaryResponse> getHealthSummary(@PathVariable UUID vehicleId) {
        return ResponseEntity.ok(diagnosticsService.getVehicleHealthSummary(vehicleId));
    }

    @PostMapping("/co-driver/chat")
    public ResponseEntity<AiAssistantResponse> askCoDriver(@RequestBody AiAssistantRequest request) {
        return ResponseEntity.ok(diagnosticsService.askCoDriver(request));
    }
}
