package de.fallstudie.minerva.backend.ticket.internal.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.context.ApplicationEventPublisher;
import de.fallstudie.minerva.backend.project.ProjectPolicies;

import de.fallstudie.minerva.backend.common.ResourceNotFoundException;
import de.fallstudie.minerva.backend.common.ValidationException;
import de.fallstudie.minerva.backend.testsupport.TestProjects;
import de.fallstudie.minerva.backend.ticket.internal.persistence.TicketChildRuleRepository;
import de.fallstudie.minerva.backend.ticket.internal.persistence.TicketCommentRepository;
import de.fallstudie.minerva.backend.ticket.internal.persistence.TicketModel;
import de.fallstudie.minerva.backend.ticket.internal.persistence.TicketRepository;
import de.fallstudie.minerva.backend.ticket.internal.persistence.TicketTypeRepository;
import de.fallstudie.minerva.backend.ticket.internal.persistence.WorkflowRepository;
import de.fallstudie.minerva.backend.ticket.internal.persistence.WorkflowStatusRepository;
import de.fallstudie.minerva.backend.ticket.internal.persistence.WorkflowTransitionRepository;

class TicketDeletionTests {
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
	void deleteTicketDeletesCommentsBeforeTicket() {
		final var ticket = TestProjects.ticket(TestProjects.CHILD_TICKET_ID,
				TestProjects.PROJECT_ID, TestProjects.CHILD_TICKET_TYPE_ID,
				TestProjects.OPEN_STATUS_ID);
		when(ticketRepository.findByIdAndProjectId(ticket.getId(), TestProjects.PROJECT_ID))
				.thenReturn(Optional.of(ticket));
		when(ticketRepository.existsByParentTicketId(ticket.getId())).thenReturn(false);

		ticketService.deleteTicket(TestProjects.PROJECT_ID, ticket.getId());

		verify(ticketCommentRepository).deleteAllByTicketId(ticket.getId());
		verify(ticketRepository).delete(ticket);
	}

	@Test
	void deleteTicketRejectsUnknownTicket() {
		when(ticketRepository.findByIdAndProjectId(TestProjects.CHILD_TICKET_ID,
				TestProjects.PROJECT_ID)).thenReturn(Optional.empty());

		assertThrows(ResourceNotFoundException.class, () -> ticketService
				.deleteTicket(TestProjects.PROJECT_ID, TestProjects.CHILD_TICKET_ID));

		verify(ticketRepository, never()).existsByParentTicketId(TestProjects.CHILD_TICKET_ID);
		verify(ticketCommentRepository, never()).deleteAllByTicketId(TestProjects.CHILD_TICKET_ID);
		verify(ticketRepository, never()).delete(Mockito.any(TicketModel.class));
	}

	@Test
	void deleteTicketRejectsTicketWithChildren() {
		final var ticket = TestProjects.ticket(TestProjects.PARENT_TICKET_ID,
				TestProjects.PROJECT_ID, TestProjects.PARENT_TICKET_TYPE_ID,
				TestProjects.OPEN_STATUS_ID);
		when(ticketRepository.findByIdAndProjectId(ticket.getId(), TestProjects.PROJECT_ID))
				.thenReturn(Optional.of(ticket));
		when(ticketRepository.existsByParentTicketId(ticket.getId())).thenReturn(true);

		assertThrows(ValidationException.class,
				() -> ticketService.deleteTicket(TestProjects.PROJECT_ID, ticket.getId()));

		verify(ticketCommentRepository, never()).deleteAllByTicketId(ticket.getId());
		verify(ticketRepository, never()).delete(Mockito.any(TicketModel.class));
	}
}
