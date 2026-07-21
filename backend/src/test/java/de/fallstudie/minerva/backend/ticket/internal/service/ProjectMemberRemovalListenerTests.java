package de.fallstudie.minerva.backend.ticket.internal.service;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.context.ApplicationEventPublisher;

import de.fallstudie.minerva.backend.project.ProjectEvent;
import de.fallstudie.minerva.backend.testsupport.TestProjects;
import de.fallstudie.minerva.backend.ticket.TicketEvent;
import de.fallstudie.minerva.backend.ticket.internal.persistence.TicketRepository;

class ProjectMemberRemovalListenerTests {
	private TicketRepository ticketRepository;
	private ApplicationEventPublisher eventPublisher;
	private ProjectMemberRemovalListener listener;

	@BeforeEach
	void setUp() {
		ticketRepository = Mockito.mock(TicketRepository.class);
		eventPublisher = Mockito.mock(ApplicationEventPublisher.class);
		listener = new ProjectMemberRemovalListener(ticketRepository, eventPublisher);
	}

	@Test
	void userRemovedUnassignsMatchingTickets() {
		final var ticket = TestProjects.ticket(TestProjects.CHILD_TICKET_ID,
				TestProjects.PROJECT_ID, TestProjects.CHILD_TICKET_TYPE_ID,
				TestProjects.OPEN_STATUS_ID);
		ticket.setAssignedTo(TestProjects.CONTRIBUTOR_USER_ID);
		final var event = new ProjectEvent.UserRemoved(TestProjects.OWNER_USER_ID,
				TestProjects.PROJECT_ID, TestProjects.CONTRIBUTOR_USER_ID);
		when(ticketRepository.findAllByProjectIdAndAssignedTo(TestProjects.PROJECT_ID,
				TestProjects.CONTRIBUTOR_USER_ID)).thenReturn(List.of(ticket));

		listener.on(event);

		assertNull(ticket.getAssignedTo());
		verify(ticketRepository).save(ticket);
		verify(eventPublisher).publishEvent(
				new TicketEvent.AssigneeChanged(TestProjects.OWNER_USER_ID, TestProjects.PROJECT_ID,
						TestProjects.CHILD_TICKET_ID, TestProjects.CONTRIBUTOR_USER_ID, null));
	}
}
