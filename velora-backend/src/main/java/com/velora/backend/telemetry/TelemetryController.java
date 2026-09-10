package com.velora.backend.telemetry;

import com.velora.backend.telemetry.dto.IngestTelemetryRequest;
import com.velora.backend.telemetry.dto.TelemetryResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/telemetry")
public class TelemetryController {

    private final TelemetryService telemetryService;

    public TelemetryController(TelemetryService telemetryService) {
        this.telemetryService = telemetryService;
    }

    @PostMapping("/ingest")
    public ResponseEntity<TelemetryResponse> ingest(@Valid @RequestBody IngestTelemetryRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(telemetryService.ingest(request));
    }

    @GetMapping("/vehicle/{vehicleId}/latest")
    public ResponseEntity<TelemetryResponse> getLatest(@PathVariable UUID vehicleId) {
        TelemetryResponse response = telemetryService.getLatestForVehicle(vehicleId);
        if (response == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(response);
    }

    @GetMapping("/vehicle/{vehicleId}/recent")
    public ResponseEntity<List<TelemetryResponse>> getRecent(@PathVariable UUID vehicleId) {
        return ResponseEntity.ok(telemetryService.getRecentForVehicle(vehicleId));
    }

    @GetMapping("/trip/{tripId}")
    public ResponseEntity<List<TelemetryResponse>> getTripTelemetry(@PathVariable UUID tripId) {
        return ResponseEntity.ok(telemetryService.getForTrip(tripId));
    }
}
