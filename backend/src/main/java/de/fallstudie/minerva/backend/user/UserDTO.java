package de.fallstudie.minerva.backend.user;

public record UserDTO(long id, String username, String passwordHash,
		WorkspaceRoleName workspaceRole) {
}
