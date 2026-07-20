package de.fallstudie.minerva.backend.statistics.internal.service;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import de.fallstudie.minerva.backend.project.IProjectStatisticsService;
import de.fallstudie.minerva.backend.ticket.ITicketStatisticsService;
import de.fallstudie.minerva.backend.ticket.TicketPriorityName;
import de.fallstudie.minerva.backend.ticket.TicketStatusCategory;
import de.fallstudie.minerva.backend.statistics.internal.web.DashboardCategoryCount;
import de.fallstudie.minerva.backend.statistics.internal.web.DashboardPriorityRow;
import de.fallstudie.minerva.backend.statistics.internal.web.DashboardRecentTicket;
import de.fallstudie.minerva.backend.statistics.internal.web.DashboardResponse;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DashboardService {
	private static final int RECENT_TICKETS_LIMIT = 5;

	private final ITicketStatisticsService ticketStatisticsService;
	private final IProjectStatisticsService projectStatisticsService;

	public DashboardResponse getProjectDashboard(long projectId) {
		return buildDashboard(List.of(projectId), Map.of());
	}

	public DashboardResponse getGlobalDashboard(long userId) {
		final var projects = projectStatisticsService.getProjectsForUser(userId);
		if (projects.isEmpty()) {
			return new DashboardResponse(0L, new DashboardCategoryCount(0L, 0L, 0L), List.of(),
					List.of());
		}

		final var projectNames = projects.stream()
				.collect(Collectors.toMap(project -> project.id(), project -> project.name()));
		final var projectIds = projects.stream().map(project -> project.id()).toList();
		return buildDashboard(projectIds, projectNames);
	}

	private DashboardResponse buildDashboard(Collection<Long> projectIds,
			Map<Long, String> projectNames) {
		final var priorityRows = List.of(TicketPriorityName.values()).stream().map(priority -> {
			final var open = count(projectIds, priority, TicketStatusCategory.OPEN);
			final var inProgress = count(projectIds, priority, TicketStatusCategory.IN_PROGRESS);
			final var completed = count(projectIds, priority, TicketStatusCategory.COMPLETED);
			return new DashboardPriorityRow(priority, open, inProgress, completed,
					open + inProgress + completed);
		}).toList();
		final var recentTickets = ticketStatisticsService
				.getRecentTickets(projectIds, RECENT_TICKETS_LIMIT).stream()
				.map(ticket -> new DashboardRecentTicket(ticket.id(), ticket.projectId(),
						projectNames.get(ticket.projectId()), ticket.name(), ticket.priority(),
						ticket.statusName(), ticket.statusCategory(), ticket.createdAt()))
				.toList();
		final var categoryCounts = new DashboardCategoryCount(
				priorityRows.stream().mapToLong(DashboardPriorityRow::open).sum(),
				priorityRows.stream().mapToLong(DashboardPriorityRow::inProgress).sum(),
				priorityRows.stream().mapToLong(DashboardPriorityRow::completed).sum());
		final var total = priorityRows.stream().mapToLong(DashboardPriorityRow::total).sum();

		return new DashboardResponse(total, categoryCounts, priorityRows, recentTickets);
	}

	private long count(Collection<Long> projectIds, TicketPriorityName priority,
			TicketStatusCategory category) {
		return ticketStatisticsService.getTicketCount(projectIds, priority, category).count();
	}
}
