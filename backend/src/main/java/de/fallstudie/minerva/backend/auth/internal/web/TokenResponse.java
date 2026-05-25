package de.fallstudie.minerva.backend.auth.internal.web;

public record TokenResponse(String accessToken, String refreshToken) {
}
