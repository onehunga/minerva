package de.fallstudie.minerva.backend.auth;

public record TokenResponse(String accessToken, String refreshToken) {
}
