package de.fallstudie.minerva.backend.statistics.internal.web;

import java.util.List;

public record DashboardResponse(long totalTickets, DashboardCategoryCount ticketsByCategory,
		List<DashboardPriorityRow> priorities, List<DashboardRecentTicket> recentTickets) {
}
