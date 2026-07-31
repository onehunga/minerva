package de.fallstudie.minerva.backend.ticket.internal.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import de.fallstudie.minerva.backend.ticket.TicketStatusCategory;

@DataJpaTest(properties = {"spring.jpa.hibernate.ddl-auto=create-drop",
		"spring.liquibase.enabled=false"})
@ActiveProfiles("test")
class WorkflowTransitionRepositoryTests {
	@Autowired
	private WorkflowStatusRepository workflowStatusRepository;

	@Autowired
	private WorkflowTransitionRepository workflowTransitionRepository;

	@Test
	void findAllForWorkflowStatesReturnsSpecificAndAnyTransitionsForGivenStates() {
		final var open = saveStatus(10L, "Open", TicketStatusCategory.OPEN);
		final var inProgress = saveStatus(10L, "In Progress", TicketStatusCategory.IN_PROGRESS);
		final var done = saveStatus(10L, "Done", TicketStatusCategory.COMPLETED);
		final var foreign = saveStatus(11L, "Foreign", TicketStatusCategory.OPEN);
		final var specificTransition = saveTransition("Start", open.getId(), inProgress.getId());
		final var anyTransition = saveTransition("Close from any", null, done.getId());
		saveTransition("Foreign", foreign.getId(), foreign.getId());

		final var transitions = workflowTransitionRepository
				.findAllForWorkflowStates(List.of(open.getId(), inProgress.getId(), done.getId()));

		assertEquals(List.of(specificTransition, anyTransition), transitions);
	}

	private WorkflowStatusModel saveStatus(long workflowId, String name,
			TicketStatusCategory category) {
		final var status = new WorkflowStatusModel();
		status.setWorkflowId(workflowId);
		status.setName(name);
		status.setWorkflowStatusCategory(category);
		return workflowStatusRepository.saveAndFlush(status);
	}

	private WorkflowTransitionModel saveTransition(String name, Long fromState, long toState) {
		final var transition = new WorkflowTransitionModel();
		transition.setName(name);
		transition.setFromState(fromState);
		transition.setToState(toState);
		return workflowTransitionRepository.saveAndFlush(transition);
	}
}
