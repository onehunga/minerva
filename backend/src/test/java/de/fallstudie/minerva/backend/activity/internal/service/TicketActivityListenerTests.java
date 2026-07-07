package de.fallstudie.minerva.backend.activity.internal.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import de.fallstudie.minerva.backend.activity.ActivityEventType;
import de.fallstudie.minerva.backend.activity.ActivityScopeType;
import de.fallstudie.minerva.backend.testsupport.TestProjects;
import de.fallstudie.minerva.backend.ticket.TicketEvent;

class TicketActivityListenerTests {
	private ActivityService activityService;
	private TicketActivityListener listener;

	@BeforeEach
	void setUp() {
		activityService = Mockito.mock(ActivityService.class);
		listener = new TicketActivityListener(activityService);
	}

	@Test
	void statusChangedAppendsTicketAndProjectScopedActivity() {
		final var event = new TicketEvent.StatusChanged(TestProjects.OWNER_USER_ID,
				TestProjects.PROJECT_ID, TestProjects.CHILD_TICKET_ID, TestProjects.OPEN_STATUS_ID,
				"Open", TestProjects.IN_PROGRESS_STATUS_ID, "In Progress",
				TestProjects.START_PROGRESS_TRANSITION_ID, "Start");

		listener.on(event);

		final var typeCaptor = ArgumentCaptor.forClass(ActivityEventType.class);
		final var eventCaptor = ArgumentCaptor.forClass(TicketEvent.class);
		final var scopesCaptor = ArgumentCaptor.forClass(List.class);
		verify(activityService).append(typeCaptor.capture(), eventCaptor.capture(),
				scopesCaptor.capture());

		assertEquals(ActivityEventType.TICKET_STATUS_CHANGED, typeCaptor.getValue());
		assertEquals(event, eventCaptor.getValue());
		assertEquals(List.of(
				new ActivityScopeCommand(ActivityScopeType.PROJECT, TestProjects.PROJECT_ID),
				new ActivityScopeCommand(ActivityScopeType.TICKET, TestProjects.CHILD_TICKET_ID)),
				scopesCaptor.getValue());
	}
}
