package de.fallstudie.minerva.backend.ticket.internal.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

import de.fallstudie.minerva.backend.common.ValidationException;
import de.fallstudie.minerva.backend.ticket.internal.persistence.TicketChildRuleModel;
import de.fallstudie.minerva.backend.ticket.internal.persistence.TicketChildRuleRepository;
import de.fallstudie.minerva.backend.ticket.internal.persistence.TicketModel;
import de.fallstudie.minerva.backend.ticket.internal.persistence.TicketRepository;
import de.fallstudie.minerva.backend.ticket.internal.persistence.TicketTypeModel;
import de.fallstudie.minerva.backend.ticket.internal.persistence.TicketTypeRepository;
import de.fallstudie.minerva.backend.ticket.internal.persistence.WorkflowModel;
import de.fallstudie.minerva.backend.ticket.internal.persistence.WorkflowRepository;
import de.fallstudie.minerva.backend.ticket.internal.persistence.WorkflowStatusCategory;
import de.fallstudie.minerva.backend.ticket.internal.persistence.WorkflowStatusModel;
import de.fallstudie.minerva.backend.ticket.internal.persistence.WorkflowStatusRepository;
import de.fallstudie.minerva.backend.ticket.internal.persistence.WorkflowTransitionRepository;
import de.fallstudie.minerva.backend.ticket.internal.persistence.WorkflowTransitionModel;
import de.fallstudie.minerva.backend.ticket.internal.web.CreateTicketRequest;
import de.fallstudie.minerva.backend.ticket.internal.web.UpdateTicketStatusRequest;
import de.fallstudie.minerva.backend.user.Identity;

class TicketServiceTests {
	private static final Identity IDENTITY = new Identity(42L);
	private static final long PROJECT_ID = 7L;

	private TicketTypeRepository ticketTypeRepository;
	private TicketChildRuleRepository ticketChildRuleRepository;
	private WorkflowRepository workflowRepository;
	private WorkflowStatusRepository workflowStatusRepository;
	private WorkflowTransitionRepository workflowTransitionRepository;
	private TicketRepository ticketRepository;
	private TicketService ticketService;

	@BeforeEach
	void setUp() {
		ticketTypeRepository = Mockito.mock(TicketTypeRepository.class);
		ticketChildRuleRepository = Mockito.mock(TicketChildRuleRepository.class);
		workflowRepository = Mockito.mock(WorkflowRepository.class);
		workflowStatusRepository = Mockito.mock(WorkflowStatusRepository.class);
		workflowTransitionRepository = Mockito.mock(WorkflowTransitionRepository.class);
		ticketRepository = Mockito.mock(TicketRepository.class);
		ticketService = new TicketService(ticketTypeRepository, ticketChildRuleRepository,
				workflowRepository, workflowStatusRepository, workflowTransitionRepository,
				ticketRepository);
	}

	@Test
	void createTicketRejectsChildTypeWhenParentDoesNotAllowIt() {
		final var parentTicket = ticket(11L, PROJECT_ID, 1L);
		final var childTicketType = ticketType(2L, "Bug");
		final var workflow = workflow(5L, PROJECT_ID, childTicketType.getId());
		final var status = workflowStatus(20L, workflow.getId());

		when(ticketTypeRepository.findByIdAndProjectId(childTicketType.getId(), PROJECT_ID))
				.thenReturn(Optional.of(childTicketType));
		when(workflowRepository.findByProjectIdAndTicketTypeId(PROJECT_ID, childTicketType.getId()))
				.thenReturn(Optional.of(workflow));
		when(workflowStatusRepository.findByIdAndWorkflowId(status.getId(), workflow.getId()))
				.thenReturn(Optional.of(status));
		when(ticketRepository.findByIdAndProjectId(parentTicket.getId(), PROJECT_ID))
				.thenReturn(Optional.of(parentTicket));
		when(ticketChildRuleRepository.findByParentTicketIdAndChildTicketId(
				parentTicket.getTicketTypeId(), childTicketType.getId()))
				.thenReturn(Optional.empty());

		assertThrows(ValidationException.class,
				() -> ticketService.createTicket(IDENTITY, PROJECT_ID, createTicketRequest(
						childTicketType.getId(), status.getId(), parentTicket.getId())));

		verify(ticketRepository, never()).save(any());
	}

	@Test
	void createTicketAcceptsAllowedChildType() {
		final var parentTicket = ticket(11L, PROJECT_ID, 1L);
		final var childTicketType = ticketType(2L, "Bug");
		final var workflow = workflow(5L, PROJECT_ID, childTicketType.getId());
		final var status = workflowStatus(20L, workflow.getId());

		when(ticketTypeRepository.findByIdAndProjectId(childTicketType.getId(), PROJECT_ID))
				.thenReturn(Optional.of(childTicketType));
		when(workflowRepository.findByProjectIdAndTicketTypeId(PROJECT_ID, childTicketType.getId()))
				.thenReturn(Optional.of(workflow));
		when(workflowStatusRepository.findByIdAndWorkflowId(status.getId(), workflow.getId()))
				.thenReturn(Optional.of(status));
		when(ticketRepository.findByIdAndProjectId(parentTicket.getId(), PROJECT_ID))
				.thenReturn(Optional.of(parentTicket));
		when(ticketChildRuleRepository.findByParentTicketIdAndChildTicketId(
				parentTicket.getTicketTypeId(), childTicketType.getId()))
				.thenReturn(Optional.of(new TicketChildRuleModel()));
		when(ticketRepository.save(any(TicketModel.class)))
				.thenAnswer(invocation -> invocation.getArgument(0));

		final var response = ticketService.createTicket(IDENTITY, PROJECT_ID,
				createTicketRequest(childTicketType.getId(), status.getId(), parentTicket.getId()));

		assertEquals(PROJECT_ID, response.projectId());
		assertEquals(childTicketType.getId(), response.ticketTypeId());
		assertEquals(status.getId(), response.statusId());
		assertEquals(parentTicket.getId(), response.parentTicketId());

		final ArgumentCaptor<TicketModel> ticketCaptor = ArgumentCaptor.forClass(TicketModel.class);
		verify(ticketRepository).save(ticketCaptor.capture());
		assertEquals(parentTicket.getId(), ticketCaptor.getValue().getParentTicketId());
		assertEquals(childTicketType.getId(), ticketCaptor.getValue().getTicketTypeId());
	}

	@Test
	void updateTicketStatusAcceptsWildcardTransitionFromAnyState() {
		final var ticketType = ticketType(1L, "Bug");
		final var workflow = workflow(5L, PROJECT_ID, ticketType.getId());
		final var openStatus = workflowStatus(20L, workflow.getId());
		final var doneStatus = workflowStatus(30L, workflow.getId());
		doneStatus.setName("Done");
		doneStatus.setWorkflowStatusCategory(WorkflowStatusCategory.COMPLETED);
		final var ticket = ticket(11L, PROJECT_ID, ticketType.getId());
		ticket.setStatusId(openStatus.getId());
		final var wildcardTransition = workflowTransition(40L, null, doneStatus.getId());

		when(ticketRepository.findByIdAndProjectId(ticket.getId(), PROJECT_ID))
				.thenReturn(Optional.of(ticket));
		when(workflowRepository.findByProjectIdAndTicketTypeId(PROJECT_ID, ticketType.getId()))
				.thenReturn(Optional.of(workflow));
		when(workflowTransitionRepository.findById(wildcardTransition.getId()))
				.thenReturn(Optional.of(wildcardTransition));
		when(workflowStatusRepository.findByIdAndWorkflowId(doneStatus.getId(), workflow.getId()))
				.thenReturn(Optional.of(doneStatus));
		when(ticketRepository.save(any(TicketModel.class)))
				.thenAnswer(invocation -> invocation.getArgument(0));

		ticketService.updateTicketStatus(PROJECT_ID, ticket.getId(),
				new UpdateTicketStatusRequest(wildcardTransition.getId()));

		final ArgumentCaptor<TicketModel> ticketCaptor = ArgumentCaptor.forClass(TicketModel.class);
		verify(ticketRepository).save(ticketCaptor.capture());
		assertEquals(doneStatus.getId(), ticketCaptor.getValue().getStatusId());
	}

	@Test
	void updateTicketStatusRejectsSpecificTransitionWhenFromDoesNotMatch() {
		final var ticketType = ticketType(1L, "Bug");
		final var workflow = workflow(5L, PROJECT_ID, ticketType.getId());
		final var openStatus = workflowStatus(20L, workflow.getId());
		final var inProgressStatus = workflowStatus(30L, workflow.getId());
		inProgressStatus.setName("In Progress");
		inProgressStatus.setWorkflowStatusCategory(WorkflowStatusCategory.IN_PROGRESS);
		final var ticket = ticket(11L, PROJECT_ID, ticketType.getId());
		ticket.setStatusId(openStatus.getId());
		final var specificTransition = workflowTransition(40L, inProgressStatus.getId(),
				openStatus.getId());

		when(ticketRepository.findByIdAndProjectId(ticket.getId(), PROJECT_ID))
				.thenReturn(Optional.of(ticket));
		when(workflowRepository.findByProjectIdAndTicketTypeId(PROJECT_ID, ticketType.getId()))
				.thenReturn(Optional.of(workflow));
		when(workflowTransitionRepository.findById(specificTransition.getId()))
				.thenReturn(Optional.of(specificTransition));
		when(workflowStatusRepository.findByIdAndWorkflowId(openStatus.getId(), workflow.getId()))
				.thenReturn(Optional.of(openStatus));

		assertThrows(ValidationException.class, () -> ticketService.updateTicketStatus(PROJECT_ID,
				ticket.getId(), new UpdateTicketStatusRequest(specificTransition.getId())));

		verify(ticketRepository, never()).save(any());
	}

	@Test
	void getTicketTypesIncludesWildcardTransitionsScopedByToState() {
		final var ticketType = ticketType(1L, "Bug");
		final var workflow = workflow(5L, PROJECT_ID, ticketType.getId());
		final var openStatus = workflowStatus(20L, workflow.getId());
		final var doneStatus = workflowStatus(30L, workflow.getId());
		doneStatus.setName("Done");
		doneStatus.setWorkflowStatusCategory(WorkflowStatusCategory.COMPLETED);
		final var wildcardTransition = workflowTransition(40L, null, doneStatus.getId());

		when(ticketTypeRepository.findAllByProjectIdOrderByNameAsc(PROJECT_ID))
				.thenReturn(List.of(ticketType));
		when(ticketChildRuleRepository.findAllByParentTicketIdOrderByIdAsc(ticketType.getId()))
				.thenReturn(List.of());
		when(workflowRepository.findByProjectIdAndTicketTypeId(PROJECT_ID, ticketType.getId()))
				.thenReturn(Optional.of(workflow));
		when(workflowStatusRepository.findAllByWorkflowIdOrderByIdAsc(workflow.getId()))
				.thenReturn(List.of(openStatus, doneStatus));
		when(workflowTransitionRepository
				.findAllForWorkflowStates(List.of(openStatus.getId(), doneStatus.getId())))
				.thenReturn(List.of(wildcardTransition));

		final var response = ticketService.getTicketTypes(PROJECT_ID);
		final var ticketTypeResponse = response.ticketTypes().getFirst();

		assertEquals(1, ticketTypeResponse.transitions().size());
		assertEquals(wildcardTransition.getId(), ticketTypeResponse.transitions().getFirst().id());
		assertEquals(null, ticketTypeResponse.transitions().getFirst().fromStateId());
		assertEquals(doneStatus.getId(), ticketTypeResponse.transitions().getFirst().toStateId());
	}

	@Test
	void getTicketTypesIncludesAllowedChildTicketIds() {
		final var parentTicketType = ticketType(1L, "Parent");
		final var childTicketType = ticketType(2L, "Child");

		when(ticketTypeRepository.findAllByProjectIdOrderByNameAsc(PROJECT_ID))
				.thenReturn(List.of(childTicketType, parentTicketType));
		when(ticketChildRuleRepository
				.findAllByParentTicketIdOrderByIdAsc(parentTicketType.getId()))
				.thenReturn(List.of(childRule(parentTicketType.getId(), childTicketType.getId())));
		when(ticketChildRuleRepository.findAllByParentTicketIdOrderByIdAsc(childTicketType.getId()))
				.thenReturn(List.of());
		when(workflowRepository.findByProjectIdAndTicketTypeId(PROJECT_ID,
				parentTicketType.getId())).thenReturn(Optional.empty());
		when(workflowRepository.findByProjectIdAndTicketTypeId(PROJECT_ID, childTicketType.getId()))
				.thenReturn(Optional.empty());

		final var response = ticketService.getTicketTypes(PROJECT_ID);

		final var parentResponse = response.ticketTypes().stream()
				.filter(ticketType -> ticketType.id() == parentTicketType.getId()).findFirst()
				.orElseThrow();
		final var childResponse = response.ticketTypes().stream()
				.filter(ticketType -> ticketType.id() == childTicketType.getId()).findFirst()
				.orElseThrow();

		assertEquals(List.of(childTicketType.getId()), parentResponse.children());
		assertEquals(List.of(), childResponse.children());
	}

	private static CreateTicketRequest createTicketRequest(long ticketTypeId, long statusId,
			Long parentTicketId) {
		return new CreateTicketRequest("Ticket", "Beschreibung", ticketTypeId, statusId,
				parentTicketId);
	}

	private static TicketTypeModel ticketType(long id, String name) {
		final var ticketType = new TicketTypeModel();
		ReflectionTestUtils.setField(ticketType, "id", id);
		ticketType.setProjectId(PROJECT_ID);
		ticketType.setName(name);
		ticketType.setDescription(name + " description");
		return ticketType;
	}

	private static WorkflowModel workflow(long id, long projectId, long ticketTypeId) {
		final var workflow = new WorkflowModel();
		ReflectionTestUtils.setField(workflow, "id", id);
		workflow.setProjectId(projectId);
		workflow.setTicketTypeId(ticketTypeId);
		return workflow;
	}

	private static WorkflowStatusModel workflowStatus(long id, long workflowId) {
		final var status = new WorkflowStatusModel();
		ReflectionTestUtils.setField(status, "id", id);
		status.setWorkflowId(workflowId);
		status.setName("Offen");
		status.setWorkflowStatusCategory(WorkflowStatusCategory.OPEN);
		return status;
	}

	private static TicketModel ticket(long id, long projectId, long ticketTypeId) {
		final var ticket = new TicketModel();
		ReflectionTestUtils.setField(ticket, "id", id);
		ticket.setProjectId(projectId);
		ticket.setTicketTypeId(ticketTypeId);
		ticket.setStatusId(1L);
		ticket.setName("Parent");
		ticket.setDescription("Beschreibung");
		ticket.setCreatedBy(IDENTITY.userId());
		return ticket;
	}

	private static TicketChildRuleModel childRule(long parentTicketId, long childTicketId) {
		final var childRule = new TicketChildRuleModel();
		childRule.setParentTicketId(parentTicketId);
		childRule.setChildTicketId(childTicketId);
		return childRule;
	}

	private static WorkflowTransitionModel workflowTransition(long id, Long fromStateId,
			long toStateId) {
		final var transition = new WorkflowTransitionModel();
		ReflectionTestUtils.setField(transition, "id", id);
		transition.setName("Transition");
		transition.setFromState(fromStateId);
		transition.setToState(toStateId);
		return transition;
	}
}
