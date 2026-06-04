package de.fallstudie.minerva.backend.ticket;

import java.util.List;

public record WorkflowConfigurationsCreateRequest(List<TicketTypeConfigurationRequest> tickets) {
}
