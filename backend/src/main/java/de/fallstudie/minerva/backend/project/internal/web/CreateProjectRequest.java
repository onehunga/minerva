package de.fallstudie.minerva.backend.project.internal.web;

import de.fallstudie.minerva.backend.ticket.WorkflowConfigurationsCreateRequest;

public record CreateProjectRequest(String name, String description,
		WorkflowConfigurationsCreateRequest workflowConfiguration) {
}
