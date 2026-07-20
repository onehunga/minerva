package de.fallstudie.minerva.backend.project.internal.web;

import de.fallstudie.minerva.backend.project.internal.persistence.ProjectRoleName;

public record ProjectDetailsResponse(long id, String name, String description,
		ProjectRoleName projectRole, boolean archived) {
}
