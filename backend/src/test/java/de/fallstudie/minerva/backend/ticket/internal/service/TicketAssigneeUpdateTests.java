package de.fallstudie.minerva.backend.ticket.internal.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import de.fallstudie.minerva.backend.project.ProjectPolicies;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

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
import de.fallstudie.minerva.backend.ticket.internal.web.UpdateTicketAssigneeRequest;

class TicketAssigneeUpdateTests {
	private ProjectPolicies projectPolicies;
	private TicketTypeRepository ticketTypeRepository;
	private TicketChildRuleRepository ticketChildRuleRepository;
	private WorkflowRepository workflowRepository;
	private WorkflowStatusRepository workflowStatusRepository;
	private WorkflowTransitionRepository workflowTransitionRepository;
	private TicketRepository ticketRepository;
	private TicketCommentRepository ticketCommentRepository;
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
		ticketService = new TicketService(projectPolicies, ticketTypeRepository,
				ticketChildRuleRepository, workflowRepository, workflowStatusRepository,
				workflowTransitionRepository, ticketRepository, ticketCommentRepository);
	}

	@Test
	void updateTicketAssigneeSavesMemberAssignee() {
		final var ticket = ticket();
		when(ticketRepository.findByIdAndProjectId(ticket.getId(), TestProjects.PROJECT_ID))
				.thenReturn(Optional.of(ticket));
		when(projectPolicies.canBeAssigned(TestProjects.PROJECT_ID,
				TestProjects.CONTRIBUTOR_USER_ID)).thenReturn(true);

		ticketService.updateTicketAssignee(TestProjects.PROJECT_ID, ticket.getId(),
				new UpdateTicketAssigneeRequest(TestProjects.CONTRIBUTOR_USER_ID));

		verify(projectPolicies).canBeAssigned(TestProjects.PROJECT_ID,
				TestProjects.CONTRIBUTOR_USER_ID);
		final var ticketCaptor = ArgumentCaptor.forClass(TicketModel.class);
		verify(ticketRepository).save(ticketCaptor.capture());
		assertEquals(TestProjects.CONTRIBUTOR_USER_ID, ticketCaptor.getValue().getAssignedTo());
	}

	@Test
	void updateTicketAssigneeAcceptsNullAssignee() {
		final var ticket = ticket();
		ticket.setAssignedTo(TestProjects.CONTRIBUTOR_USER_ID);
		when(ticketRepository.findByIdAndProjectId(ticket.getId(), TestProjects.PROJECT_ID))
				.thenReturn(Optional.of(ticket));

		ticketService.updateTicketAssignee(TestProjects.PROJECT_ID, ticket.getId(),
				new UpdateTicketAssigneeRequest(null));

		verify(projectPolicies, never()).canBeAssigned(Mockito.anyLong(), Mockito.anyLong());
		final var ticketCaptor = ArgumentCaptor.forClass(TicketModel.class);
		verify(ticketRepository).save(ticketCaptor.capture());
		assertNull(ticketCaptor.getValue().getAssignedTo());
	}

	@Test
	void updateTicketAssigneeRejectsUnknownTicket() {
		when(ticketRepository.findByIdAndProjectId(TestProjects.CHILD_TICKET_ID,
				TestProjects.PROJECT_ID)).thenReturn(Optional.empty());

		assertThrows(ResourceNotFoundException.class,
				() -> ticketService.updateTicketAssignee(TestProjects.PROJECT_ID,
						TestProjects.CHILD_TICKET_ID,
						new UpdateTicketAssigneeRequest(TestProjects.CONTRIBUTOR_USER_ID)));

		verify(ticketRepository, never()).save(any());
	}

	@Test
	void updateTicketAssigneeRejectsNullRequest() {
		assertThrows(IllegalArgumentException.class, () -> ticketService
				.updateTicketAssignee(TestProjects.PROJECT_ID, TestProjects.CHILD_TICKET_ID, null));

		verify(ticketRepository, never()).save(any());
	}

	@Test
	void updateTicketAssigneeRejectsNonEligibleAssignee() {
		final var ticket = ticket();
		when(ticketRepository.findByIdAndProjectId(ticket.getId(), TestProjects.PROJECT_ID))
				.thenReturn(Optional.of(ticket));
		Mockito.doThrow(
				new ValidationException("Zugewiesener Nutzer ist kein Mitglied des Projekts"))
				.when(projectPolicies)
				.canBeAssigned(TestProjects.PROJECT_ID, TestProjects.CONTRIBUTOR_USER_ID);

		assertThrows(ValidationException.class,
				() -> ticketService.updateTicketAssignee(TestProjects.PROJECT_ID, ticket.getId(),
						new UpdateTicketAssigneeRequest(TestProjects.CONTRIBUTOR_USER_ID)));

		verify(ticketRepository, never()).save(any());
	}

	private TicketModel ticket() {
		return TestProjects.ticket(TestProjects.CHILD_TICKET_ID, TestProjects.PROJECT_ID,
				TestProjects.CHILD_TICKET_TYPE_ID, TestProjects.OPEN_STATUS_ID);
	}
}
