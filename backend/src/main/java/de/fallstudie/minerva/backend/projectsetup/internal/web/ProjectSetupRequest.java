package de.fallstudie.minerva.backend.projectsetup.internal.web;

import de.fallstudie.minerva.backend.ticket.WorkflowConfigurationsCreateRequest;

public record ProjectSetupRequest(String name, String description,
		WorkflowConfigurationsCreateRequest ticketConfiguration) {
}
