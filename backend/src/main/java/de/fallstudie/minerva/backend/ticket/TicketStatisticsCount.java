package de.fallstudie.minerva.backend.ticket;

public record TicketStatisticsCount(TicketPriorityName priority, TicketStatusCategory category,
		long count) {
}
