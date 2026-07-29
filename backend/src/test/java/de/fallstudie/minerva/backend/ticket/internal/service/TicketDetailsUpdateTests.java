package de.fallstudie.minerva.backend.ticket.internal.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.time.Instant;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.context.ApplicationEventPublisher;
import de.fallstudie.minerva.backend.project.ProjectPolicies;

import de.fallstudie.minerva.backend.common.ResourceNotFoundException;
import de.fallstudie.minerva.backend.common.ReadOnlyException;
import de.fallstudie.minerva.backend.common.ValidationException;
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
import de.fallstudie.minerva.backend.ticket.internal.web.UpdateTicketDetailsRequest;

class TicketDetailsUpdateTests {
	private TicketTypeRepository ticketTypeRepository;
	private TicketChildRuleRepository ticketChildRuleRepository;
	private WorkflowRepository workflowRepository;
	private WorkflowStatusRepository workflowStatusRepository;
	private WorkflowTransitionRepository workflowTransitionRepository;
	private TicketRepository ticketRepository;
	private TicketCommentRepository ticketCommentRepository;
	private ApplicationEventPublisher eventPublisher;
	private ProjectPolicies projectPolicies;
	private TicketService ticketService;

	@BeforeEach
	void setUp() {
		projectPolicies = Mockito.mock(ProjectPolicies.class);
		ticketTypeRepository = Mockito.mock(TicketTypeRepository.class);
		ticketChildRuleRepository = Mockito.mock(TicketChildRuleRepository.class);
		workflowRepository = Mockito.mock(WorkflowRepository.class);
		workflowStatusRepository = Mockito.mock(WorkflowStatusRepository.class);
		workflowTransitionRepository = Mockito.mock(WorkflowTransitionRepository.class);
		ticketRepository = Mockito.mock(TicketRepository.class);
		ticketCommentRepository = Mockito.mock(TicketCommentRepository.class);
		eventPublisher = Mockito.mock(ApplicationEventPublisher.class);
		ticketService = new TicketService(projectPolicies, ticketTypeRepository,
				ticketChildRuleRepository, workflowRepository, workflowStatusRepository,
				workflowTransitionRepository, ticketRepository, ticketCommentRepository,
				eventPublisher);
	}

	@Test
	void updateTicketDetailsSavesTrimmedDetails() {
		final var ticket = ticket();
		when(ticketRepository.findByIdAndProjectId(ticket.getId(), TestProjects.PROJECT_ID))
				.thenReturn(Optional.of(ticket));

		ticketService.updateTicketDetails(TestProjects.OWNER, TestProjects.PROJECT_ID,
				ticket.getId(),
				new UpdateTicketDetailsRequest("  Neuer Name  ", "  Neue Beschreibung  "));

		final var ticketCaptor = ArgumentCaptor.forClass(TicketModel.class);
		verify(ticketRepository).save(ticketCaptor.capture());
		assertEquals("Neuer Name", ticketCaptor.getValue().getName());
		assertEquals("Neue Beschreibung", ticketCaptor.getValue().getDescription());
		verify(eventPublisher).publishEvent(new TicketEvent.DetailsUpdated(
				TestProjects.OWNER_USER_ID, TestProjects.PROJECT_ID, ticket.getId(), "Ticket 41",
				"Neuer Name", "Description 41", "Neue Beschreibung", null));
	}

	@Test
	void updateTicketDetailsAcceptsNullDescription() {
		final var ticket = ticket();
		when(ticketRepository.findByIdAndProjectId(ticket.getId(), TestProjects.PROJECT_ID))
				.thenReturn(Optional.of(ticket));

		ticketService.updateTicketDetails(TestProjects.OWNER, TestProjects.PROJECT_ID,
				ticket.getId(), new UpdateTicketDetailsRequest("Neuer Name", null));

		final var ticketCaptor = ArgumentCaptor.forClass(TicketModel.class);
		verify(ticketRepository).save(ticketCaptor.capture());
		assertEquals("", ticketCaptor.getValue().getDescription());
	}

	@Test
	void updateTicketDetailsRejectsUnknownTicket() {
		when(ticketRepository.findByIdAndProjectId(TestProjects.CHILD_TICKET_ID,
				TestProjects.PROJECT_ID)).thenReturn(Optional.empty());

		assertThrows(ResourceNotFoundException.class,
				() -> ticketService.updateTicketDetails(TestProjects.OWNER, TestProjects.PROJECT_ID,
						TestProjects.CHILD_TICKET_ID,
						new UpdateTicketDetailsRequest("Neuer Name", "Beschreibung")));

		verify(ticketRepository, never()).save(any());
	}

	@Test
	void updateTicketDetailsRejectsArchivedTicket() {
		final var ticket = ticket();
		ticket.setArchivedAt(Instant.now());
		when(ticketRepository.findByIdAndProjectId(ticket.getId(), TestProjects.PROJECT_ID))
				.thenReturn(Optional.of(ticket));

		assertThrows(ReadOnlyException.class,
				() -> ticketService.updateTicketDetails(TestProjects.OWNER, TestProjects.PROJECT_ID,
						ticket.getId(), new UpdateTicketDetailsRequest("Neu", "Beschreibung")));

		verify(ticketRepository, never()).save(any());
		verify(eventPublisher, never()).publishEvent(any());
	}

	@ParameterizedTest
	@MethodSource("invalidUpdateTicketDetailsRequests")
	void updateTicketDetailsRejectsInvalidRequest(UpdateTicketDetailsRequest request) {
		assertThrows(ValidationException.class,
				() -> ticketService.updateTicketDetails(TestProjects.OWNER, TestProjects.PROJECT_ID,
						TestProjects.CHILD_TICKET_ID, request));

		verify(ticketRepository, never()).save(any());
	}

	@Test
	void updateTicketDetailsRejectsNullRequest() {
		assertThrows(IllegalArgumentException.class,
				() -> ticketService.updateTicketDetails(TestProjects.OWNER, TestProjects.PROJECT_ID,
						TestProjects.CHILD_TICKET_ID, null));

		verify(ticketRepository, never()).save(any());
	}

	private TicketModel ticket() {
		return TestProjects.ticket(TestProjects.CHILD_TICKET_ID, TestProjects.PROJECT_ID,
				TestProjects.CHILD_TICKET_TYPE_ID, TestProjects.OPEN_STATUS_ID);
	}

	private static Stream<UpdateTicketDetailsRequest> invalidUpdateTicketDetailsRequests() {
		return Stream.of(new UpdateTicketDetailsRequest(" ", "Beschreibung"),
				new UpdateTicketDetailsRequest("a".repeat(256), "Beschreibung"),
				new UpdateTicketDetailsRequest("Name", "a".repeat(256)));
	}
}
