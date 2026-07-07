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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
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
import de.fallstudie.minerva.backend.ticket.internal.web.UpdateTicketStatusRequest;

class TicketStatusUpdateTests {
	private TicketTypeRepository ticketTypeRepository;
	private TicketChildRuleRepository ticketChildRuleRepository;
	private WorkflowRepository workflowRepository;
	private WorkflowStatusRepository workflowStatusRepository;
	private WorkflowTransitionRepository workflowTransitionRepository;
	private TicketRepository ticketRepository;
	private TicketCommentRepository ticketCommentRepository;
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
		ticketService = new TicketService(projectPolicies, ticketTypeRepository,
				ticketChildRuleRepository, workflowRepository, workflowStatusRepository,
				workflowTransitionRepository, ticketRepository, ticketCommentRepository);
	}

	@Test
	void updateTicketStatusAcceptsSpecificTransitionFromCurrentState() {
		final var ticket = TestProjects.ticket(TestProjects.CHILD_TICKET_ID,
				TestProjects.PROJECT_ID, TestProjects.CHILD_TICKET_TYPE_ID,
				TestProjects.OPEN_STATUS_ID);
		final var workflow = TestProjects.workflow(TestProjects.WORKFLOW_ID,
				TestProjects.PROJECT_ID, ticket.getTicketTypeId());
		final var transition = TestProjects.startProgressTransition();
		final var targetStatus = TestProjects.inProgressStatus(workflow.getId());
		stubTicketWorkflowTransitionAndTargetStatus(ticket, workflow, transition.getId(),
				transition, targetStatus.getId());

		ticketService.updateTicketStatus(TestProjects.PROJECT_ID, ticket.getId(),
				new UpdateTicketStatusRequest(transition.getId()));

		final var ticketCaptor = ArgumentCaptor.forClass(TicketModel.class);
		verify(ticketRepository).save(ticketCaptor.capture());
		assertEquals(TestProjects.IN_PROGRESS_STATUS_ID, ticketCaptor.getValue().getStatusId());
	}

	@Test
	void updateTicketStatusAcceptsAnyTransitionFromAnyCurrentState() {
		final var ticket = TestProjects.ticket(TestProjects.CHILD_TICKET_ID,
				TestProjects.PROJECT_ID, TestProjects.CHILD_TICKET_TYPE_ID,
				TestProjects.OPEN_STATUS_ID);
		final var workflow = TestProjects.workflow(TestProjects.WORKFLOW_ID,
				TestProjects.PROJECT_ID, ticket.getTicketTypeId());
		final var transition = TestProjects.anyDoneTransition();
		final var targetStatus = TestProjects.doneStatus(workflow.getId());
		stubTicketWorkflowTransitionAndTargetStatus(ticket, workflow, transition.getId(),
				transition, targetStatus.getId());

		ticketService.updateTicketStatus(TestProjects.PROJECT_ID, ticket.getId(),
				new UpdateTicketStatusRequest(transition.getId()));

		final var ticketCaptor = ArgumentCaptor.forClass(TicketModel.class);
		verify(ticketRepository).save(ticketCaptor.capture());
		assertEquals(TestProjects.DONE_STATUS_ID, ticketCaptor.getValue().getStatusId());
	}

	@Test
	void updateTicketStatusRejectsSpecificTransitionWhenFromDoesNotMatch() {
		final var ticket = TestProjects.ticket(TestProjects.CHILD_TICKET_ID,
				TestProjects.PROJECT_ID, TestProjects.CHILD_TICKET_TYPE_ID,
				TestProjects.DONE_STATUS_ID);
		final var workflow = TestProjects.workflow(TestProjects.WORKFLOW_ID,
				TestProjects.PROJECT_ID, ticket.getTicketTypeId());
		final var transition = TestProjects.startProgressTransition();
		final var targetStatus = TestProjects.inProgressStatus(workflow.getId());
		stubTicketWorkflowTransitionAndTargetStatus(ticket, workflow, transition.getId(),
				transition, targetStatus.getId());

		assertThrows(ValidationException.class,
				() -> ticketService.updateTicketStatus(TestProjects.PROJECT_ID, ticket.getId(),
						new UpdateTicketStatusRequest(transition.getId())));

		verify(ticketRepository, never()).save(any());
	}

	@Test
	void updateTicketStatusRejectsUnknownTicket() {
		when(ticketRepository.findByIdAndProjectId(TestProjects.CHILD_TICKET_ID,
				TestProjects.PROJECT_ID)).thenReturn(Optional.empty());

		assertThrows(ResourceNotFoundException.class,
				() -> ticketService.updateTicketStatus(TestProjects.PROJECT_ID,
						TestProjects.CHILD_TICKET_ID,
						new UpdateTicketStatusRequest(TestProjects.START_PROGRESS_TRANSITION_ID)));

		verify(ticketRepository, never()).save(any());
	}

	@Test
	void updateTicketStatusRejectsTicketTypeWithoutWorkflow() {
		final var ticket = TestProjects.ticket(TestProjects.CHILD_TICKET_ID,
				TestProjects.PROJECT_ID, TestProjects.CHILD_TICKET_TYPE_ID,
				TestProjects.OPEN_STATUS_ID);
		when(ticketRepository.findByIdAndProjectId(ticket.getId(), TestProjects.PROJECT_ID))
				.thenReturn(Optional.of(ticket));
		when(workflowRepository.findByProjectIdAndTicketTypeId(TestProjects.PROJECT_ID,
				ticket.getTicketTypeId())).thenReturn(Optional.empty());

		assertThrows(ValidationException.class,
				() -> ticketService.updateTicketStatus(TestProjects.PROJECT_ID, ticket.getId(),
						new UpdateTicketStatusRequest(TestProjects.START_PROGRESS_TRANSITION_ID)));

		verify(ticketRepository, never()).save(any());
	}

	@Test
	void updateTicketStatusRejectsUnknownTransition() {
		final var ticket = TestProjects.ticket(TestProjects.CHILD_TICKET_ID,
				TestProjects.PROJECT_ID, TestProjects.CHILD_TICKET_TYPE_ID,
				TestProjects.OPEN_STATUS_ID);
		final var workflow = TestProjects.workflow(TestProjects.WORKFLOW_ID,
				TestProjects.PROJECT_ID, ticket.getTicketTypeId());
		when(ticketRepository.findByIdAndProjectId(ticket.getId(), TestProjects.PROJECT_ID))
				.thenReturn(Optional.of(ticket));
		when(workflowRepository.findByProjectIdAndTicketTypeId(TestProjects.PROJECT_ID,
				ticket.getTicketTypeId())).thenReturn(Optional.of(workflow));
		when(workflowTransitionRepository.findById(TestProjects.START_PROGRESS_TRANSITION_ID))
				.thenReturn(Optional.empty());

		assertThrows(ValidationException.class,
				() -> ticketService.updateTicketStatus(TestProjects.PROJECT_ID, ticket.getId(),
						new UpdateTicketStatusRequest(TestProjects.START_PROGRESS_TRANSITION_ID)));

		verify(ticketRepository, never()).save(any());
	}

	@Test
	void updateTicketStatusRejectsTargetStatusOutsideWorkflow() {
		final var ticket = TestProjects.ticket(TestProjects.CHILD_TICKET_ID,
				TestProjects.PROJECT_ID, TestProjects.CHILD_TICKET_TYPE_ID,
				TestProjects.OPEN_STATUS_ID);
		final var workflow = TestProjects.workflow(TestProjects.WORKFLOW_ID,
				TestProjects.PROJECT_ID, ticket.getTicketTypeId());
		final var transition = TestProjects.startProgressTransition();
		when(ticketRepository.findByIdAndProjectId(ticket.getId(), TestProjects.PROJECT_ID))
				.thenReturn(Optional.of(ticket));
		when(workflowRepository.findByProjectIdAndTicketTypeId(TestProjects.PROJECT_ID,
				ticket.getTicketTypeId())).thenReturn(Optional.of(workflow));
		when(workflowTransitionRepository.findById(transition.getId()))
				.thenReturn(Optional.of(transition));
		when(workflowStatusRepository.findByIdAndWorkflowId(transition.getToState(),
				workflow.getId())).thenReturn(Optional.empty());

		assertThrows(ValidationException.class,
				() -> ticketService.updateTicketStatus(TestProjects.PROJECT_ID, ticket.getId(),
						new UpdateTicketStatusRequest(transition.getId())));

		verify(ticketRepository, never()).save(any());
	}

	@ParameterizedTest
	@ValueSource(longs = {0L, -1L})
	void updateTicketStatusRejectsInvalidTransitionId(long transitionId) {
		assertThrows(ValidationException.class,
				() -> ticketService.updateTicketStatus(TestProjects.PROJECT_ID,
						TestProjects.CHILD_TICKET_ID, new UpdateTicketStatusRequest(transitionId)));

		verify(ticketRepository, never()).save(any());
	}

	private void stubTicketWorkflowTransitionAndTargetStatus(TicketModel ticket,
			de.fallstudie.minerva.backend.ticket.internal.persistence.WorkflowModel workflow,
			long transitionId,
			de.fallstudie.minerva.backend.ticket.internal.persistence.WorkflowTransitionModel transition,
			long targetStatusId) {
		when(ticketRepository.findByIdAndProjectId(ticket.getId(), TestProjects.PROJECT_ID))
				.thenReturn(Optional.of(ticket));
		when(workflowRepository.findByProjectIdAndTicketTypeId(TestProjects.PROJECT_ID,
				ticket.getTicketTypeId())).thenReturn(Optional.of(workflow));
		when(workflowTransitionRepository.findById(transitionId))
				.thenReturn(Optional.of(transition));
		when(workflowStatusRepository.findByIdAndWorkflowId(targetStatusId, workflow.getId()))
				.thenReturn(Optional.of(TestProjects.workflowStatus(targetStatusId,
						workflow.getId(), "Target",
						de.fallstudie.minerva.backend.ticket.internal.persistence.WorkflowStatusCategory.IN_PROGRESS)));
	}
}
