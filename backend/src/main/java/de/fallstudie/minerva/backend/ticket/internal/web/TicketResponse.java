package de.fallstudie.minerva.backend.ticket.internal.web;

import java.time.Instant;
import java.util.List;

import de.fallstudie.minerva.backend.ticket.TicketPriorityName;

public record TicketResponse(long id, long projectId, long ticketTypeId, long statusId,
		TicketPriorityName priority, Long parentTicketId, String name, String description,
		List<TicketChildResponse> children, long createdBy, Long assignedTo, Instant createdAt,
		Instant updatedAt, boolean archived) {
}
