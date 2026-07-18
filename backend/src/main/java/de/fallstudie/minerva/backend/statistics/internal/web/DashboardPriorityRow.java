package de.fallstudie.minerva.backend.statistics.internal.web;

import de.fallstudie.minerva.backend.ticket.TicketPriorityName;

public record DashboardPriorityRow(TicketPriorityName priority, long open, long inProgress,
		long completed, long total) {
}
