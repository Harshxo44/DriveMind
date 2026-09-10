package com.velora.backend.diagnostics;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface DiagnosticTroubleCodeRepository extends JpaRepository<DiagnosticTroubleCode, UUID> {
    Optional<DiagnosticTroubleCode> findByCodeIgnoreCase(String code);
}
