package de.fallstudie.minerva.backend.ticket.internal.web;

import java.time.Instant;

public record TicketResponse(long id, long projectId, long ticketTypeId, long statusId, String name,
		String description, long createdBy, Long assignedTo, Instant createdAt, Instant updatedAt) {
}
