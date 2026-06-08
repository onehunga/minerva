package de.fallstudie.minerva.backend.ticket.internal;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

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
	void rejectsDuplicateWildcardTransitionToSameTarget() {
		final var validator = validatorWithTransitions(List.of(
				new TicketStateTransitionConfigurationRequest("Schliessen", null, "Done"),
				new TicketStateTransitionConfigurationRequest("Auch schliessen", null, "Done")));

		assertThrows(DuplicateResourceException.class, validator::validate);
	}

	@Test
	void rejectsEmptyStates() {
		final var ticket = new TicketTypeConfigurationRequest("Bug", "Bugs", List.of(), List.of(),
				List.of());
		final var validator = new WorkflowConfigurationsValidator(
				new WorkflowConfigurationsCreateRequest(List.of(ticket)));

		assertThrows(IllegalArgumentException.class, validator::validate);
	}

	@Test
	void rejectsDuplicateStateNames() {
		final var ticket = new TicketTypeConfigurationRequest("Bug", "Bugs",
				List.of(new TicketStateConfigurationRequest("Open", "OPEN"),
						new TicketStateConfigurationRequest("Open", "IN_PROGRESS")),
				List.of(), List.of());
		final var validator = new WorkflowConfigurationsValidator(
				new WorkflowConfigurationsCreateRequest(List.of(ticket)));

		assertThrows(DuplicateResourceException.class, validator::validate);
	}

	@ParameterizedTest
	@MethodSource("invalidTransitions")
	void rejectsInvalidTransitions(TicketStateTransitionConfigurationRequest transition) {
		final var validator = validatorWithTransitions(List.of(transition));

		assertThrows(ValidationException.class, validator::validate);
	}

	@Test
	void rejectsUnknownChildTicketType() {
		final var ticket = new TicketTypeConfigurationRequest("Epic", "Epics",
				List.of(new TicketStateConfigurationRequest("Open", "OPEN")), List.of(),
				List.of("Missing"));
		final var validator = new WorkflowConfigurationsValidator(
				new WorkflowConfigurationsCreateRequest(List.of(ticket)));

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

	private static List<TicketStateTransitionConfigurationRequest> invalidTransitions() {
		return List.of(new TicketStateTransitionConfigurationRequest("Schliessen", null, "Missing"),
				new TicketStateTransitionConfigurationRequest("Loop", "Open", "Open"),
				new TicketStateTransitionConfigurationRequest("Start", "Missing", "Done"));
	}
}
