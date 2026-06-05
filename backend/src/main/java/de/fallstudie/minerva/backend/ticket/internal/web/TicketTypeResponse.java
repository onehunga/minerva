package de.fallstudie.minerva.backend.ticket.internal.web;

import java.util.List;

public record TicketTypeResponse(long id, String name, String description,
		List<WorkflowStateResponse> states, List<WorkflowTransitionResponse> transitions) {
}
