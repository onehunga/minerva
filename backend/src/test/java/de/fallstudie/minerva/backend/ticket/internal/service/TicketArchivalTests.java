package de.fallstudie.minerva.backend.ticket.internal.service;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.time.Instant;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.context.ApplicationEventPublisher;

import de.fallstudie.minerva.backend.common.ResourceNotFoundException;
import de.fallstudie.minerva.backend.common.ReadOnlyException;
import de.fallstudie.minerva.backend.project.ProjectPolicies;
import de.fallstudie.minerva.backend.testsupport.TestProjects;
import de.fallstudie.minerva.backend.ticket.TicketEvent;
import de.fallstudie.minerva.backend.ticket.internal.persistence.TicketChildRuleRepository;
import de.fallstudie.minerva.backend.ticket.internal.persistence.TicketCommentRepository;
import de.fallstudie.minerva.backend.ticket.internal.persistence.TicketModel;
import de.fallstudie.minerva.backend.ticket.internal.persistence.TicketRepository;
import de.fallstudie.minerva.backend.ticket.internal.persistence.TicketTypeRepository;
import de.fallstudie.minerva.backend.ticket.internal.persistence.WorkflowRepository;
import de.fallstudie.minerva.backend.ticket.internal.persistence.WorkflowStatusRepository;
import de.fallstudie.minerva.backend.ticket.internal.persistence.WorkflowTransitionRepository;

class TicketArchivalTests {
	private TicketRepository ticketRepository;
	private ApplicationEventPublisher eventPublisher;
	private ProjectPolicies projectPolicies;
	private TicketService ticketService;

	@BeforeEach
	void setUp() {
		ticketRepository = Mockito.mock(TicketRepository.class);
		eventPublisher = Mockito.mock(ApplicationEventPublisher.class);
		projectPolicies = Mockito.mock(ProjectPolicies.class);
		ticketService = new TicketService(projectPolicies, Mockito.mock(TicketTypeRepository.class),
				Mockito.mock(TicketChildRuleRepository.class),
				Mockito.mock(WorkflowRepository.class),
				Mockito.mock(WorkflowStatusRepository.class),
				Mockito.mock(WorkflowTransitionRepository.class), ticketRepository,
				Mockito.mock(TicketCommentRepository.class), eventPublisher);
	}

	@Test
	void archiveTicketArchivesExistingTicket() {
		final var ticket = TestProjects.ticket(TestProjects.CHILD_TICKET_ID,
				TestProjects.PROJECT_ID, TestProjects.CHILD_TICKET_TYPE_ID,
				TestProjects.OPEN_STATUS_ID);
		when(ticketRepository.findByIdAndProjectId(ticket.getId(), TestProjects.PROJECT_ID))
				.thenReturn(Optional.of(ticket));

		ticketService.archiveTicket(TestProjects.OWNER, TestProjects.PROJECT_ID, ticket.getId());

		assertNotNull(ticket.getArchivedAt());
		verify(ticketRepository).save(ticket);
		verify(ticketRepository).flush();
		verify(eventPublisher)
				.publishEvent(new TicketEvent.TicketArchived(TestProjects.OWNER_USER_ID,
						TestProjects.PROJECT_ID, ticket.getId(), ticket.getName()));
	}

	@Test
	void archiveTicketRejectsUnknownTicket() {
		when(ticketRepository.findByIdAndProjectId(TestProjects.CHILD_TICKET_ID,
				TestProjects.PROJECT_ID)).thenReturn(Optional.empty());

		assertThrows(ResourceNotFoundException.class,
				() -> ticketService.archiveTicket(TestProjects.OWNER, TestProjects.PROJECT_ID,
						TestProjects.CHILD_TICKET_ID));

		verify(ticketRepository, never()).save(Mockito.any(TicketModel.class));
		verify(ticketRepository, never()).flush();
		verify(eventPublisher, never()).publishEvent(Mockito.any());
	}

	@Test
	void archiveTicketDoesNothingForArchivedTicket() {
		final var ticket = TestProjects.ticket(TestProjects.CHILD_TICKET_ID,
				TestProjects.PROJECT_ID, TestProjects.CHILD_TICKET_TYPE_ID,
				TestProjects.OPEN_STATUS_ID);
		ticket.setArchivedAt(Instant.now());
		when(ticketRepository.findByIdAndProjectId(ticket.getId(), TestProjects.PROJECT_ID))
				.thenReturn(Optional.of(ticket));

		ticketService.archiveTicket(TestProjects.OWNER, TestProjects.PROJECT_ID, ticket.getId());

		verify(ticketRepository, never()).save(Mockito.any());
		verify(ticketRepository, never()).flush();
		verify(eventPublisher, never()).publishEvent(Mockito.any());
	}

	@Test
	void archiveTicketRejectsArchivedProject() {
		when(projectPolicies.isArchived(TestProjects.PROJECT_ID)).thenReturn(true);

		assertThrows(ReadOnlyException.class, () -> ticketService.archiveTicket(TestProjects.OWNER,
				TestProjects.PROJECT_ID, TestProjects.CHILD_TICKET_ID));

		verify(ticketRepository, never()).save(Mockito.any());
		verify(eventPublisher, never()).publishEvent(Mockito.any());
	}
}
