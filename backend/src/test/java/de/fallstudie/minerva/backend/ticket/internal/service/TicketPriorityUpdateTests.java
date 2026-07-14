package de.fallstudie.minerva.backend.ticket.internal.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.context.ApplicationEventPublisher;
import de.fallstudie.minerva.backend.project.ProjectPolicies;

import de.fallstudie.minerva.backend.common.ResourceNotFoundException;
import de.fallstudie.minerva.backend.common.ValidationException;
import de.fallstudie.minerva.backend.testsupport.TestProjects;
import de.fallstudie.minerva.backend.ticket.TicketPriorityName;
import de.fallstudie.minerva.backend.ticket.TicketEvent;
import de.fallstudie.minerva.backend.ticket.internal.persistence.TicketChildRuleRepository;
import de.fallstudie.minerva.backend.ticket.internal.persistence.TicketCommentRepository;
import de.fallstudie.minerva.backend.ticket.internal.persistence.TicketModel;
import de.fallstudie.minerva.backend.ticket.internal.persistence.TicketRepository;
import de.fallstudie.minerva.backend.ticket.internal.persistence.TicketTypeRepository;
import de.fallstudie.minerva.backend.ticket.internal.persistence.WorkflowRepository;
import de.fallstudie.minerva.backend.ticket.internal.persistence.WorkflowStatusRepository;
import de.fallstudie.minerva.backend.ticket.internal.persistence.WorkflowTransitionRepository;
import de.fallstudie.minerva.backend.ticket.internal.web.UpdateTicketPriorityRequest;

class TicketPriorityUpdateTests {
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
	void updateTicketPrioritySavesPriority() {
		final var ticket = TestProjects.ticket(TestProjects.CHILD_TICKET_ID,
				TestProjects.PROJECT_ID, TestProjects.CHILD_TICKET_TYPE_ID,
				TestProjects.OPEN_STATUS_ID);
		when(ticketRepository.findByIdAndProjectId(ticket.getId(), TestProjects.PROJECT_ID))
				.thenReturn(Optional.of(ticket));

		ticketService.updateTicketPriority(TestProjects.OWNER, TestProjects.PROJECT_ID,
				ticket.getId(), new UpdateTicketPriorityRequest(TicketPriorityName.HIGH));

		final var ticketCaptor = ArgumentCaptor.forClass(TicketModel.class);
		verify(ticketRepository).save(ticketCaptor.capture());
		assertEquals(TicketPriorityName.HIGH, ticketCaptor.getValue().getPriority());
		verify(eventPublisher).publishEvent(
				new TicketEvent.PriorityChanged(TestProjects.OWNER_USER_ID, TestProjects.PROJECT_ID,
						ticket.getId(), TicketPriorityName.NORMAL, TicketPriorityName.HIGH));
	}

	@Test
	void updateTicketPriorityRejectsUnknownTicket() {
		when(ticketRepository.findByIdAndProjectId(TestProjects.CHILD_TICKET_ID,
				TestProjects.PROJECT_ID)).thenReturn(Optional.empty());

		assertThrows(ResourceNotFoundException.class,
				() -> ticketService.updateTicketPriority(TestProjects.OWNER,
						TestProjects.PROJECT_ID, TestProjects.CHILD_TICKET_ID,
						new UpdateTicketPriorityRequest(TicketPriorityName.HIGH)));

		verify(ticketRepository, never()).save(any());
	}

	@Test
	void updateTicketPriorityRejectsNullPriority() {
		assertThrows(ValidationException.class,
				() -> ticketService.updateTicketPriority(TestProjects.OWNER,
						TestProjects.PROJECT_ID, TestProjects.CHILD_TICKET_ID,
						new UpdateTicketPriorityRequest(null)));

		verify(ticketRepository, never()).save(any());
	}

	@Test
	void updateTicketPriorityRejectsNullRequest() {
		assertThrows(IllegalArgumentException.class,
				() -> ticketService.updateTicketPriority(TestProjects.OWNER,
						TestProjects.PROJECT_ID, TestProjects.CHILD_TICKET_ID, null));

		verify(ticketRepository, never()).save(any());
	}
}
