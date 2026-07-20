package de.fallstudie.minerva.backend.ticket.internal.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.context.ApplicationEventPublisher;
import de.fallstudie.minerva.backend.project.ProjectPolicies;

import de.fallstudie.minerva.backend.testsupport.TestProjects;
import de.fallstudie.minerva.backend.ticket.internal.persistence.TicketChildRuleRepository;
import de.fallstudie.minerva.backend.ticket.internal.persistence.TicketCommentRepository;
import de.fallstudie.minerva.backend.ticket.internal.persistence.TicketRepository;
import de.fallstudie.minerva.backend.ticket.internal.persistence.TicketTypeRepository;
import de.fallstudie.minerva.backend.ticket.internal.persistence.WorkflowRepository;
import de.fallstudie.minerva.backend.ticket.internal.persistence.WorkflowStatusRepository;
import de.fallstudie.minerva.backend.ticket.internal.persistence.WorkflowTransitionRepository;

class TicketTypeQueryTests {
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
	void getTicketTypesIncludesStatesTransitionsAndAnyTransitions() {
		final var ticketType = TestProjects.ticketType(TestProjects.CHILD_TICKET_TYPE_ID, "Task");
		final var workflow = TestProjects.workflow(TestProjects.WORKFLOW_ID,
				TestProjects.PROJECT_ID, ticketType.getId());
		final var openStatus = TestProjects.openStatus(workflow.getId());
		final var inProgressStatus = TestProjects.inProgressStatus(workflow.getId());
		final var doneStatus = TestProjects.doneStatus(workflow.getId());
		final var startProgressTransition = TestProjects.startProgressTransition();
		final var anyDoneTransition = TestProjects.anyDoneTransition();
		when(ticketTypeRepository.findAllByProjectIdOrderByNameAsc(TestProjects.PROJECT_ID))
				.thenReturn(List.of(ticketType));
		when(ticketChildRuleRepository.findAllByParentTicketIdOrderByIdAsc(ticketType.getId()))
				.thenReturn(List.of());
		when(workflowRepository.findByProjectIdAndTicketTypeId(TestProjects.PROJECT_ID,
				ticketType.getId())).thenReturn(Optional.of(workflow));
		when(workflowStatusRepository.findAllByWorkflowIdOrderByIdAsc(workflow.getId()))
				.thenReturn(List.of(openStatus, inProgressStatus, doneStatus));
		when(workflowTransitionRepository.findAllForWorkflowStates(
				List.of(openStatus.getId(), inProgressStatus.getId(), doneStatus.getId())))
				.thenReturn(List.of(startProgressTransition, anyDoneTransition));

		final var response = ticketService.getTicketTypes(TestProjects.PROJECT_ID);
		final var ticketTypeResponse = response.ticketTypes().getFirst();

		assertEquals(ticketType.getId(), ticketTypeResponse.id());
		assertEquals(3, ticketTypeResponse.states().size());
		assertEquals(2, ticketTypeResponse.transitions().size());
		assertEquals(TestProjects.OPEN_STATUS_ID,
				ticketTypeResponse.transitions().getFirst().fromStateId());
		assertEquals(TestProjects.IN_PROGRESS_STATUS_ID,
				ticketTypeResponse.transitions().getFirst().toStateId());
		assertEquals(null, ticketTypeResponse.transitions().get(1).fromStateId());
		assertEquals(TestProjects.DONE_STATUS_ID,
				ticketTypeResponse.transitions().get(1).toStateId());
	}

	@Test
	void getTicketTypesIncludesAllowedChildTicketIds() {
		final var parentTicketType = TestProjects.ticketType(TestProjects.PARENT_TICKET_TYPE_ID,
				"Epic");
		final var childTicketType = TestProjects.ticketType(TestProjects.CHILD_TICKET_TYPE_ID,
				"Task");
		when(ticketTypeRepository.findAllByProjectIdOrderByNameAsc(TestProjects.PROJECT_ID))
				.thenReturn(List.of(childTicketType, parentTicketType));
		when(ticketChildRuleRepository
				.findAllByParentTicketIdOrderByIdAsc(parentTicketType.getId()))
				.thenReturn(List.of(
						TestProjects.childRule(parentTicketType.getId(), childTicketType.getId())));
		when(ticketChildRuleRepository.findAllByParentTicketIdOrderByIdAsc(childTicketType.getId()))
				.thenReturn(List.of());
		when(workflowRepository.findByProjectIdAndTicketTypeId(TestProjects.PROJECT_ID,
				parentTicketType.getId())).thenReturn(Optional.empty());
		when(workflowRepository.findByProjectIdAndTicketTypeId(TestProjects.PROJECT_ID,
				childTicketType.getId())).thenReturn(Optional.empty());

		final var response = ticketService.getTicketTypes(TestProjects.PROJECT_ID);

		final var parentResponse = response.ticketTypes().stream()
				.filter(ticketType -> ticketType.id() == parentTicketType.getId()).findFirst()
				.orElseThrow();
		final var childResponse = response.ticketTypes().stream()
				.filter(ticketType -> ticketType.id() == childTicketType.getId()).findFirst()
				.orElseThrow();
		assertEquals(List.of(childTicketType.getId()), parentResponse.children());
		assertEquals(List.of(), childResponse.children());
	}
}
