package de.fallstudie.minerva.backend.user.internal.web;

public record CreateUserRequest(String username, String password, String role) {
}
