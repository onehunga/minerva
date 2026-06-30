package de.fallstudie.minerva.backend.ticket.internal.web;

import java.time.Instant;

public record TicketCommentResponse(long id, long ticketId, long authorId, String authorUsername,
		String content, Instant createdAt, Instant updatedAt) {
}
