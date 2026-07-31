package de.fallstudie.minerva.backend.project.internal.web;

import de.fallstudie.minerva.backend.project.internal.persistence.ProjectRoleName;

public record ProjectRecordResponse(long id, String name, String description,
		ProjectRoleName projectRole, long openTicketCount, long ticketCount) {
}
