package de.fallstudie.minerva.backend.statistics.internal.web;

import java.time.Instant;

import de.fallstudie.minerva.backend.ticket.TicketPriorityName;
import de.fallstudie.minerva.backend.ticket.TicketStatusCategory;

public record DashboardRecentTicket(long id, long projectId, String projectName, String name,
		TicketPriorityName priority, String statusName, TicketStatusCategory statusCategory,
		Instant createdAt) {
}
