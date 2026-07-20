package de.fallstudie.minerva.backend.ticket;

import java.time.Instant;

public record TicketStatisticsRecentTicket(long id, long projectId, String name,
		TicketPriorityName priority, String statusName, TicketStatusCategory statusCategory,
		Instant createdAt) {
}
