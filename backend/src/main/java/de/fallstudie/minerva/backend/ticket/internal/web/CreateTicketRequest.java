package de.fallstudie.minerva.backend.ticket.internal.web;

import de.fallstudie.minerva.backend.ticket.TicketPriorityName;

public record CreateTicketRequest(String name, String description, long ticketTypeId, long statusId,
		Long parentTicketId, TicketPriorityName priority, Long assignedTo) {
}
