package de.fallstudie.minerva.backend.ticket.internal.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import de.fallstudie.minerva.backend.common.ResourceNotFoundException;
import de.fallstudie.minerva.backend.common.ValidationException;
import de.fallstudie.minerva.backend.testsupport.TestProjects;
import de.fallstudie.minerva.backend.ticket.TicketPriorityName;
import de.fallstudie.minerva.backend.ticket.internal.persistence.TicketChildRuleModel;
import de.fallstudie.minerva.backend.ticket.internal.persistence.TicketChildRuleRepository;
import de.fallstudie.minerva.backend.ticket.internal.persistence.TicketModel;
import de.fallstudie.minerva.backend.ticket.internal.persistence.TicketRepository;
import de.fallstudie.minerva.backend.ticket.internal.persistence.TicketTypeModel;
import de.fallstudie.minerva.backend.ticket.internal.persistence.TicketTypeRepository;
import de.fallstudie.minerva.backend.ticket.internal.persistence.WorkflowModel;
import de.fallstudie.minerva.backend.ticket.internal.persistence.WorkflowRepository;
import de.fallstudie.minerva.backend.ticket.internal.persistence.WorkflowStatusRepository;
import de.fallstudie.minerva.backend.ticket.internal.persistence.WorkflowTransitionRepository;
import de.fallstudie.minerva.backend.ticket.internal.web.CreateTicketRequest;

class TicketCreationTests {
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
	void createTicketSavesRootTicketWithTrimmedTextAndCreator() {
		final var ticketType = TestProjects.ticketType(TestProjects.PARENT_TICKET_TYPE_ID, "Epic");
		final var workflow = TestProjects.workflow(TestProjects.WORKFLOW_ID,
				TestProjects.PROJECT_ID, ticketType.getId());
		final var status = TestProjects.openStatus(workflow.getId());
		stubValidTypeWorkflowAndStatus(ticketType, workflow);
		when(workflowStatusRepository.findByIdAndWorkflowId(status.getId(), workflow.getId()))
				.thenReturn(Optional.of(status));
		when(ticketRepository.save(any(TicketModel.class)))
				.thenAnswer(invocation -> invocation.getArgument(0));

		final var response = ticketService.createTicket(TestProjects.OWNER, TestProjects.PROJECT_ID,
				new CreateTicketRequest("  Root ticket  ", "  Beschreibung  ", ticketType.getId(),
						status.getId(), null));

		assertEquals(TestProjects.PROJECT_ID, response.projectId());
		assertEquals(ticketType.getId(), response.ticketTypeId());
		assertEquals(status.getId(), response.statusId());
		assertEquals(TicketPriorityName.NORMAL, response.priority());
		assertEquals(null, response.parentTicketId());
		final var ticketCaptor = ArgumentCaptor.forClass(TicketModel.class);
		verify(ticketRepository).save(ticketCaptor.capture());
		final var savedTicket = ticketCaptor.getValue();
		assertEquals("Root ticket", savedTicket.getName());
		assertEquals("Beschreibung", savedTicket.getDescription());
		assertEquals(TicketPriorityName.NORMAL, savedTicket.getPriority());
		assertEquals(TestProjects.OWNER_USER_ID, savedTicket.getCreatedBy());
		assertEquals(null, savedTicket.getParentTicketId());
	}

	@Test
	void createTicketAcceptsAllowedChildTypeForParentTicket() {
		final var parentTicket = TestProjects.ticket(TestProjects.PARENT_TICKET_ID,
				TestProjects.PROJECT_ID, TestProjects.PARENT_TICKET_TYPE_ID,
				TestProjects.OPEN_STATUS_ID);
		final var childTicketType = TestProjects.ticketType(TestProjects.CHILD_TICKET_TYPE_ID,
				"Task");
		final var workflow = TestProjects.workflow(TestProjects.WORKFLOW_ID,
				TestProjects.PROJECT_ID, childTicketType.getId());
		final var status = TestProjects.openStatus(workflow.getId());
		stubValidTypeWorkflowAndStatus(childTicketType, workflow);
		when(workflowStatusRepository.findByIdAndWorkflowId(status.getId(), workflow.getId()))
				.thenReturn(Optional.of(status));
		when(ticketRepository.findByIdAndProjectId(parentTicket.getId(), TestProjects.PROJECT_ID))
				.thenReturn(Optional.of(parentTicket));
		when(ticketChildRuleRepository.findByParentTicketIdAndChildTicketId(
				TestProjects.PARENT_TICKET_TYPE_ID, childTicketType.getId()))
				.thenReturn(Optional.of(new TicketChildRuleModel()));
		when(ticketRepository.save(any(TicketModel.class)))
				.thenAnswer(invocation -> invocation.getArgument(0));

		final var response = ticketService.createTicket(TestProjects.OWNER, TestProjects.PROJECT_ID,
				createTicketRequest(childTicketType.getId(), status.getId(), parentTicket.getId()));

		assertEquals(parentTicket.getId(), response.parentTicketId());
		final var ticketCaptor = ArgumentCaptor.forClass(TicketModel.class);
		verify(ticketRepository).save(ticketCaptor.capture());
		assertEquals(parentTicket.getId(), ticketCaptor.getValue().getParentTicketId());
		assertEquals(childTicketType.getId(), ticketCaptor.getValue().getTicketTypeId());
	}

	@Test
	void createTicketRejectsChildTypeWhenParentDoesNotAllowIt() {
		final var parentTicket = TestProjects.ticket(TestProjects.PARENT_TICKET_ID,
				TestProjects.PROJECT_ID, TestProjects.PARENT_TICKET_TYPE_ID,
				TestProjects.OPEN_STATUS_ID);
		final var forbiddenChildType = TestProjects
				.ticketType(TestProjects.FORBIDDEN_CHILD_TICKET_TYPE_ID, "Bug");
		final var workflow = TestProjects.workflow(TestProjects.WORKFLOW_ID,
				TestProjects.PROJECT_ID, forbiddenChildType.getId());
		final var status = TestProjects.openStatus(workflow.getId());
		stubValidTypeWorkflowAndStatus(forbiddenChildType, workflow);
		when(workflowStatusRepository.findByIdAndWorkflowId(status.getId(), workflow.getId()))
				.thenReturn(Optional.of(status));
		when(ticketRepository.findByIdAndProjectId(parentTicket.getId(), TestProjects.PROJECT_ID))
				.thenReturn(Optional.of(parentTicket));
		when(ticketChildRuleRepository.findByParentTicketIdAndChildTicketId(
				TestProjects.PARENT_TICKET_TYPE_ID, forbiddenChildType.getId()))
				.thenReturn(Optional.empty());

		assertThrows(ValidationException.class,
				() -> ticketService.createTicket(TestProjects.OWNER, TestProjects.PROJECT_ID,
						createTicketRequest(forbiddenChildType.getId(), status.getId(),
								parentTicket.getId())));

		verify(ticketRepository, never()).save(any());
	}

	@Test
	void createTicketRejectsParentTicketFromDifferentProject() {
		final var childTicketType = TestProjects.ticketType(TestProjects.CHILD_TICKET_TYPE_ID,
				"Task");
		final var workflow = TestProjects.workflow(TestProjects.WORKFLOW_ID,
				TestProjects.PROJECT_ID, childTicketType.getId());
		final var status = TestProjects.openStatus(workflow.getId());
		stubValidTypeWorkflowAndStatus(childTicketType, workflow);
		when(workflowStatusRepository.findByIdAndWorkflowId(status.getId(), workflow.getId()))
				.thenReturn(Optional.of(status));
		when(ticketRepository.findByIdAndProjectId(TestProjects.PARENT_TICKET_ID,
				TestProjects.PROJECT_ID)).thenReturn(Optional.empty());

		assertThrows(ValidationException.class,
				() -> ticketService.createTicket(TestProjects.OWNER, TestProjects.PROJECT_ID,
						createTicketRequest(childTicketType.getId(), status.getId(),
								TestProjects.PARENT_TICKET_ID)));

		verify(ticketRepository, never()).save(any());
	}

	@Test
	void createTicketRejectsUnknownTicketType() {
		when(ticketTypeRepository.findByIdAndProjectId(TestProjects.PARENT_TICKET_TYPE_ID,
				TestProjects.PROJECT_ID)).thenReturn(Optional.empty());

		assertThrows(ResourceNotFoundException.class,
				() -> ticketService.createTicket(TestProjects.OWNER, TestProjects.PROJECT_ID,
						createTicketRequest(TestProjects.PARENT_TICKET_TYPE_ID,
								TestProjects.OPEN_STATUS_ID, null)));

		verify(ticketRepository, never()).save(any());
	}

	@Test
	void createTicketRejectsTicketTypeWithoutWorkflow() {
		final var ticketType = TestProjects.ticketType(TestProjects.PARENT_TICKET_TYPE_ID, "Epic");
		when(ticketTypeRepository.findByIdAndProjectId(ticketType.getId(), TestProjects.PROJECT_ID))
				.thenReturn(Optional.of(ticketType));
		when(workflowRepository.findByProjectIdAndTicketTypeId(TestProjects.PROJECT_ID,
				ticketType.getId())).thenReturn(Optional.empty());

		assertThrows(ValidationException.class, () -> ticketService.createTicket(TestProjects.OWNER,
				TestProjects.PROJECT_ID,
				createTicketRequest(ticketType.getId(), TestProjects.OPEN_STATUS_ID, null)));

		verify(ticketRepository, never()).save(any());
	}

	@Test
	void createTicketRejectsStatusOutsideTicketTypeWorkflow() {
		final var ticketType = TestProjects.ticketType(TestProjects.PARENT_TICKET_TYPE_ID, "Epic");
		final var workflow = TestProjects.workflow(TestProjects.WORKFLOW_ID,
				TestProjects.PROJECT_ID, ticketType.getId());
		stubValidTypeWorkflowAndStatus(ticketType, workflow);
		when(workflowStatusRepository.findByIdAndWorkflowId(TestProjects.OPEN_STATUS_ID,
				workflow.getId())).thenReturn(Optional.empty());

		assertThrows(ValidationException.class, () -> ticketService.createTicket(TestProjects.OWNER,
				TestProjects.PROJECT_ID,
				createTicketRequest(ticketType.getId(), TestProjects.OPEN_STATUS_ID, null)));

		verify(ticketRepository, never()).save(any());
	}

	@ParameterizedTest
	@MethodSource("invalidCreateTicketRequests")
	void createTicketRejectsInvalidRequestValues(CreateTicketRequest request) {
		assertThrows(ValidationException.class, () -> ticketService.createTicket(TestProjects.OWNER,
				TestProjects.PROJECT_ID, request));

		verify(ticketRepository, never()).save(any());
	}

	private void stubValidTypeWorkflowAndStatus(TicketTypeModel ticketType,
			WorkflowModel workflow) {
		when(ticketTypeRepository.findByIdAndProjectId(ticketType.getId(), TestProjects.PROJECT_ID))
				.thenReturn(Optional.of(ticketType));
		when(workflowRepository.findByProjectIdAndTicketTypeId(TestProjects.PROJECT_ID,
				ticketType.getId())).thenReturn(Optional.of(workflow));
	}

	private static CreateTicketRequest createTicketRequest(long ticketTypeId, long statusId,
			Long parentTicketId) {
		return new CreateTicketRequest("Ticket", "Beschreibung", ticketTypeId, statusId,
				parentTicketId);
	}

	private static Stream<CreateTicketRequest> invalidCreateTicketRequests() {
		return Stream.of(
				new CreateTicketRequest(" ", "Beschreibung", TestProjects.PARENT_TICKET_TYPE_ID,
						TestProjects.OPEN_STATUS_ID, null),
				new CreateTicketRequest("a".repeat(256), "Beschreibung",
						TestProjects.PARENT_TICKET_TYPE_ID, TestProjects.OPEN_STATUS_ID, null),
				new CreateTicketRequest("Ticket", "a".repeat(256),
						TestProjects.PARENT_TICKET_TYPE_ID, TestProjects.OPEN_STATUS_ID, null),
				new CreateTicketRequest("Ticket", "Beschreibung", 0, TestProjects.OPEN_STATUS_ID,
						null),
				new CreateTicketRequest("Ticket", "Beschreibung",
						TestProjects.PARENT_TICKET_TYPE_ID, 0, null),
				new CreateTicketRequest("Ticket", "Beschreibung",
						TestProjects.PARENT_TICKET_TYPE_ID, TestProjects.OPEN_STATUS_ID, 0L),
				new CreateTicketRequest("Ticket", "Beschreibung",
						TestProjects.PARENT_TICKET_TYPE_ID, TestProjects.OPEN_STATUS_ID, -1L));
	}
}
