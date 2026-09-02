package com.guardian.backend.auth.dto;

import java.util.UUID;

public record AuthResponse(
        String accessToken,
        String tokenType,
        UUID userId,
        String name,
        String email,
        String role
) {
    public AuthResponse(String accessToken, UUID userId, String name, String email, String role) {
        this(accessToken, "Bearer", userId, name, email, role);
    }
}