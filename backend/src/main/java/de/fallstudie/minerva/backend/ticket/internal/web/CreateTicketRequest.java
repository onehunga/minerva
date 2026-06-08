package de.fallstudie.minerva.backend.ticket.internal.web;

public record CreateTicketRequest(String name, String description, long ticketTypeId, long statusId,
		Long parentTicketId) {
}
