package de.fallstudie.minerva.backend.ticket;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.function.Function;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import de.fallstudie.minerva.backend.testsupport.TestProjects;
import de.fallstudie.minerva.backend.ticket.internal.persistence.TicketChildRuleRepository;
import de.fallstudie.minerva.backend.ticket.internal.persistence.TicketTypeModel;
import de.fallstudie.minerva.backend.ticket.internal.persistence.TicketTypeRepository;
import de.fallstudie.minerva.backend.ticket.internal.persistence.WorkflowRepository;
import de.fallstudie.minerva.backend.ticket.internal.persistence.WorkflowStatusCategory;
import de.fallstudie.minerva.backend.ticket.internal.persistence.WorkflowStatusModel;
import de.fallstudie.minerva.backend.ticket.internal.persistence.WorkflowStatusRepository;
import de.fallstudie.minerva.backend.ticket.internal.persistence.WorkflowTransitionRepository;

@DataJpaTest(properties = {"spring.jpa.hibernate.ddl-auto=create-drop",
		"spring.liquibase.enabled=false"})
@ActiveProfiles("test")
class TicketConfigurationServiceTests {
	@Autowired
	private TicketTypeRepository ticketTypeRepository;

	@Autowired
	private WorkflowRepository workflowRepository;

	@Autowired
	private WorkflowStatusRepository workflowStatusRepository;

	@Autowired
	private WorkflowTransitionRepository workflowTransitionRepository;

	@Autowired
	private TicketChildRuleRepository ticketChildRuleRepository;

	private TicketConfigurationService ticketConfigurationService;

	@BeforeEach
	void setUp() {
		ticketConfigurationService = new TicketConfigurationService(ticketTypeRepository,
				workflowRepository, workflowStatusRepository, workflowTransitionRepository,
				ticketChildRuleRepository);
	}

	@Test
	void createTicketConfigurationPersistsTicketTypesWorkflowsStatesTransitionsAndChildren() {
		ticketConfigurationService.createTicketConfiguration(TestProjects.PROJECT_ID,
				TestProjects.workflowConfiguration());

		final var ticketTypes = ticketTypeRepository
				.findAllByProjectIdOrderByNameAsc(TestProjects.PROJECT_ID);
		assertEquals(3, ticketTypes.size());
		final var ticketTypesByName = ticketTypes.stream()
				.collect(Collectors.toMap(TicketTypeModel::getName, Function.identity()));
		final var parentTicketType = ticketTypesByName.get("Epic");
		final var childTicketType = ticketTypesByName.get("Task");
		final var forbiddenChildType = ticketTypesByName.get("Bug");
		assertNotNull(parentTicketType);
		assertNotNull(childTicketType);
		assertNotNull(forbiddenChildType);

		final var parentWorkflow = workflowRepository
				.findByProjectIdAndTicketTypeId(TestProjects.PROJECT_ID, parentTicketType.getId())
				.orElseThrow();
		final var parentStates = workflowStatusRepository
				.findAllByWorkflowIdOrderByIdAsc(parentWorkflow.getId());
		assertEquals(3, parentStates.size());
		final var parentStatesByName = parentStates.stream()
				.collect(Collectors.toMap(WorkflowStatusModel::getName, Function.identity()));
		assertEquals(WorkflowStatusCategory.OPEN,
				parentStatesByName.get("Open").getWorkflowStatusCategory());
		assertEquals(WorkflowStatusCategory.IN_PROGRESS,
				parentStatesByName.get("In Progress").getWorkflowStatusCategory());
		assertEquals(WorkflowStatusCategory.COMPLETED,
				parentStatesByName.get("Done").getWorkflowStatusCategory());

		final var transitions = workflowTransitionRepository.findAllForWorkflowStates(
				parentStates.stream().map(WorkflowStatusModel::getId).toList());
		assertEquals(2, transitions.size());
		final var startTransition = transitions.stream()
				.filter(transition -> transition.getFromState() != null).findFirst().orElseThrow();
		final var anyTransition = transitions.stream()
				.filter(transition -> transition.getFromState() == null).findFirst().orElseThrow();
		assertEquals(parentStatesByName.get("Open").getId(), startTransition.getFromState());
		assertEquals(parentStatesByName.get("In Progress").getId(), startTransition.getToState());
		assertEquals(parentStatesByName.get("Done").getId(), anyTransition.getToState());

		final var parentChildRules = ticketChildRuleRepository
				.findAllByParentTicketIdOrderByIdAsc(parentTicketType.getId());
		assertEquals(1, parentChildRules.size());
		assertEquals(childTicketType.getId(), parentChildRules.getFirst().getChildTicketId());
		assertEquals(0, ticketChildRuleRepository
				.findAllByParentTicketIdOrderByIdAsc(forbiddenChildType.getId()).size());
	}
}
