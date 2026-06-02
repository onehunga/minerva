package de.fallstudie.minerva.backend.project.internal.web;

import de.fallstudie.minerva.backend.project.internal.persistence.ProjectRoleName;

public record ProjectUserResponse(long id, String username, ProjectRoleName projectRole,
		boolean member) {
}
