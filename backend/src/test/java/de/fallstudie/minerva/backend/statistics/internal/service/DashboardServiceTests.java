package de.fallstudie.minerva.backend.statistics.internal.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import de.fallstudie.minerva.backend.project.IProjectStatisticsService;
import de.fallstudie.minerva.backend.project.ProjectStatisticsProject;
import de.fallstudie.minerva.backend.ticket.ITicketStatisticsService;
import de.fallstudie.minerva.backend.ticket.TicketPriorityName;
import de.fallstudie.minerva.backend.ticket.TicketStatisticsCount;
import de.fallstudie.minerva.backend.ticket.TicketStatisticsRecentTicket;
import de.fallstudie.minerva.backend.ticket.TicketStatusCategory;

class DashboardServiceTests {
	private static final long PROJECT_ID = 7L;
	private static final long OTHER_PROJECT_ID = 8L;
	private static final long USER_ID = 42L;

	private final Map<String, Long> counts = new HashMap<>();
	private List<TicketStatisticsRecentTicket> recentTickets = List.of();
	private ITicketStatisticsService ticketStatisticsService;
	private IProjectStatisticsService projectStatisticsService;
	private DashboardService dashboardService;

	@BeforeEach
	void setUp() {
		ticketStatisticsService = Mockito.mock(ITicketStatisticsService.class);
		projectStatisticsService = Mockito.mock(IProjectStatisticsService.class);
		dashboardService = new DashboardService(ticketStatisticsService, projectStatisticsService);
		when(ticketStatisticsService.getTicketCount(any(), any(), any())).thenAnswer(invocation -> {
			final var priority = invocation.getArgument(1, TicketPriorityName.class);
			final var category = invocation.getArgument(2, TicketStatusCategory.class);
			return new TicketStatisticsCount(priority, category,
					counts.getOrDefault(key(priority, category), 0L));
		});
		when(ticketStatisticsService.getRecentTickets(any(), anyInt()))
				.thenAnswer(invocation -> recentTickets);
	}

	@Test
	void getProjectDashboardAggregatesTypedTicketStatistics() {
		count(TicketPriorityName.HIGH, TicketStatusCategory.OPEN, 2L);
		count(TicketPriorityName.HIGH, TicketStatusCategory.IN_PROGRESS, 1L);
		count(TicketPriorityName.NORMAL, TicketStatusCategory.COMPLETED, 3L);
		count(TicketPriorityName.LOW, TicketStatusCategory.OPEN, 1L);
		recentTickets = List.of(new TicketStatisticsRecentTicket(99L, PROJECT_ID, "Letztes Ticket",
				TicketPriorityName.HIGH, "In Bearbeitung", TicketStatusCategory.IN_PROGRESS,
				Instant.parse("2024-04-01T10:00:00Z")));

		final var dashboard = dashboardService.getProjectDashboard(PROJECT_ID);

		assertEquals(7L, dashboard.totalTickets());
		assertEquals(3L, dashboard.ticketsByCategory().open());
		assertEquals(1L, dashboard.ticketsByCategory().inProgress());
		assertEquals(3L, dashboard.ticketsByCategory().completed());
		assertEquals(List.of(TicketPriorityName.LOWEST, TicketPriorityName.LOW,
				TicketPriorityName.NORMAL, TicketPriorityName.HIGH, TicketPriorityName.HIGHEST),
				dashboard.priorities().stream().map(row -> row.priority()).toList());
		assertEquals(3L, dashboard.priorities().get(3).total());
		assertEquals(null, dashboard.recentTickets().getFirst().projectName());
	}

	@Test
	void getProjectDashboardReturnsZeroRowsWhenProjectHasNoTickets() {
		final var dashboard = dashboardService.getProjectDashboard(PROJECT_ID);

		assertEquals(0L, dashboard.totalTickets());
		assertEquals(5, dashboard.priorities().size());
		assertTrue(dashboard.priorities().stream().allMatch(row -> row.total() == 0L));
		assertTrue(dashboard.recentTickets().isEmpty());
	}

	@Test
	void getGlobalDashboardSkipsTicketQueriesWhenUserHasNoProjects() {
		when(projectStatisticsService.getProjectsForUser(USER_ID)).thenReturn(List.of());

		final var dashboard = dashboardService.getGlobalDashboard(USER_ID);

		assertEquals(0L, dashboard.totalTickets());
		assertTrue(dashboard.priorities().isEmpty());
		assertTrue(dashboard.recentTickets().isEmpty());
		verify(ticketStatisticsService, never()).getTicketCount(any(), any(), any());
		verify(ticketStatisticsService, never()).getRecentTickets(any(), anyInt());
	}

	@Test
	void getGlobalDashboardAddsProjectNamesToRecentTickets() {
		when(projectStatisticsService.getProjectsForUser(USER_ID))
				.thenReturn(List.of(new ProjectStatisticsProject(PROJECT_ID, "Minerva"),
						new ProjectStatisticsProject(OTHER_PROJECT_ID, "Andere")));
		count(TicketPriorityName.NORMAL, TicketStatusCategory.OPEN, 4L);
		recentTickets = List.of(
				new TicketStatisticsRecentTicket(5L, PROJECT_ID, "Alpha", TicketPriorityName.NORMAL,
						"Offen", TicketStatusCategory.OPEN, Instant.parse("2024-05-01T08:00:00Z")),
				new TicketStatisticsRecentTicket(6L, OTHER_PROJECT_ID, "Bravo",
						TicketPriorityName.NORMAL, "Offen", TicketStatusCategory.OPEN,
						Instant.parse("2024-04-30T08:00:00Z")));

		final var dashboard = dashboardService.getGlobalDashboard(USER_ID);

		assertEquals(4L, dashboard.totalTickets());
		assertEquals("Minerva", dashboard.recentTickets().getFirst().projectName());
		assertEquals("Andere", dashboard.recentTickets().get(1).projectName());
	}

	private void count(TicketPriorityName priority, TicketStatusCategory category, long value) {
		counts.put(key(priority, category), value);
	}

	private String key(TicketPriorityName priority, TicketStatusCategory category) {
		return priority + ":" + category;
	}
}
