package de.fallstudie.minerva.backend.user.internal.web;

import de.fallstudie.minerva.backend.user.WorkspaceRoleName;

public record UserRecordResponse(long id, String username, WorkspaceRoleName role,
		boolean deactivated) {
}
