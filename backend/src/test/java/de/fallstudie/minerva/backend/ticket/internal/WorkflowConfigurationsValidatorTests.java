package de.fallstudie.minerva.backend.ticket.internal;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.junit.jupiter.api.Test;

import de.fallstudie.minerva.backend.common.DuplicateResourceException;
import de.fallstudie.minerva.backend.common.ValidationException;
import de.fallstudie.minerva.backend.ticket.TicketStateConfigurationRequest;
import de.fallstudie.minerva.backend.ticket.TicketStateTransitionConfigurationRequest;
import de.fallstudie.minerva.backend.ticket.TicketTypeConfigurationRequest;
import de.fallstudie.minerva.backend.ticket.WorkflowConfigurationsCreateRequest;

class WorkflowConfigurationsValidatorTests {
	@Test
	void acceptsWildcardTransitionWithNullFrom() {
		final var validator = validatorWithTransitions(
				List.of(new TicketStateTransitionConfigurationRequest("Schliessen", null, "Done")));

		assertDoesNotThrow(validator::validate);
	}

	@Test
	void rejectsWildcardTransitionWithInvalidToState() {
		final var validator = validatorWithTransitions(List
				.of(new TicketStateTransitionConfigurationRequest("Schliessen", null, "Missing")));

		assertThrows(ValidationException.class, validator::validate);
	}

	@Test
	void rejectsDuplicateWildcardTransitionToSameTarget() {
		final var validator = validatorWithTransitions(List.of(
				new TicketStateTransitionConfigurationRequest("Schliessen", null, "Done"),
				new TicketStateTransitionConfigurationRequest("Auch schliessen", null, "Done")));

		assertThrows(DuplicateResourceException.class, validator::validate);
	}

	@Test
	void rejectsSelfLoopTransition() {
		final var validator = validatorWithTransitions(
				List.of(new TicketStateTransitionConfigurationRequest("Loop", "Open", "Open")));

		assertThrows(ValidationException.class, validator::validate);
	}

	private static WorkflowConfigurationsValidator validatorWithTransitions(
			List<TicketStateTransitionConfigurationRequest> transitions) {
		final var ticket = new TicketTypeConfigurationRequest("Bug", "Bugs",
				List.of(new TicketStateConfigurationRequest("Open", "OPEN"),
						new TicketStateConfigurationRequest("Done", "COMPLETED")),
				transitions, List.of());

		return new WorkflowConfigurationsValidator(
				new WorkflowConfigurationsCreateRequest(List.of(ticket)));
	}
}
