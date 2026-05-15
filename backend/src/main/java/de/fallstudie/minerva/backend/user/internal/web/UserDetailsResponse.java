package de.fallstudie.minerva.backend.user.internal.web;

import de.fallstudie.minerva.backend.authorization.WorkspaceRoleName;

public record UserDetailsResponse(long id, String username, WorkspaceRoleName role) {
}
