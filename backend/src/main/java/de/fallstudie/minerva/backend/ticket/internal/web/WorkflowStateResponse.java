package de.fallstudie.minerva.backend.ticket.internal.web;

import de.fallstudie.minerva.backend.ticket.TicketStatusCategory;

public record WorkflowStateResponse(long id, String name, TicketStatusCategory category) {
}
