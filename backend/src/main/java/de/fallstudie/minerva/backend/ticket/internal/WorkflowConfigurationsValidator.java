package de.fallstudie.minerva.backend.ticket.internal;

import de.fallstudie.minerva.backend.common.DuplicateResourceException;
import de.fallstudie.minerva.backend.common.ValidationException;
import de.fallstudie.minerva.backend.ticket.TicketStateConfigurationRequest;
import de.fallstudie.minerva.backend.ticket.TicketStateTransitionConfigurationRequest;
import de.fallstudie.minerva.backend.ticket.TicketTypeConfigurationRequest;
import de.fallstudie.minerva.backend.ticket.WorkflowConfigurationsCreateRequest;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
// TODO: wollen wir validierung serverseitig haben und als komplette liste ans
// FE geben?
public class WorkflowConfigurationsValidator {
	private final WorkflowConfigurationsCreateRequest req;

	public void validate() {
		req.tickets().forEach(this::validateTicketConfiguration);
	}

	private void validateTicketConfiguration(TicketTypeConfigurationRequest ticket) {
		if (ticket.name() == null || ticket.name().isBlank()) {
			throw new IllegalArgumentException("Ticket type name must not be empty");
		}
		assert ticket.description() != null : "Ticket type description must not be null";
		assert ticket.states() != null : "Ticket type states must not be null";
		assert ticket.transitions() != null : "Ticket type transitions must not be null";
		assert ticket.children() != null : "Ticket type children must not be null";

		final var stateNames = validateTicketStates(ticket.states());
		validateTicketTransitions(stateNames, ticket.transitions());
		validateChildren(ticket.children());
	}

	private Set<String> validateTicketStates(
			@NonNull List<@NonNull TicketStateConfigurationRequest> states) {
		if (states.isEmpty()) {
			throw new IllegalArgumentException("Ticket states must not be empty");
		}

		final var stateSet = states.stream().map(TicketStateConfigurationRequest::name)
				.collect(Collectors.toSet());

		if (states.size() != stateSet.size()) {
			throw new DuplicateResourceException(
					"Ticketzustände müssen einen Eindeutigen Namen haben");
		}

		// TODO: brauchen wir diesen check?
		// final var categorySet =
		// states.stream().map(TicketStateConfigurationRequest::category).collect(Collectors.toSet());
		// if (categorySet.size() != WorkflowStatusCategory.values().length) {
		// throw new ValidationException("Es muss mindestens 1 Zustand von jeweils OPEN,
		// IN_PROGRESS und CLOSED geben");
		// }

		return stateSet;
	}

	// TODO: brauchen wir mindestens einen Übergang?
	private void validateTicketTransitions(Set<String> stateNames,
			List<TicketStateTransitionConfigurationRequest> transitions) {
		for (final var transition : transitions) {
			assert transition.name() != null : "Transitions must not be null";
			assert transition.from() != null : "Transition from state must not be null";
			assert transition.to() != null : "Transition to state must not be null";

			if (!stateNames.contains(transition.from())) {
				throw new ValidationException(
						"Übergangszustand " + transition.from() + " ist kein gültiger Zustand");
			}
			if (!stateNames.contains(transition.to())) {
				throw new ValidationException(
						"Übergangszustand " + transition.to() + " ist kein gültiger Zustand");
			}
		}
	}

	private void validateChildren(List<String> children) {
		for (final var child : children) {
			req.tickets().stream().filter(t -> t.name().equals(child)).findFirst()
					.orElseThrow(() -> new ValidationException(
							"Ticket " + child + " ist kein Gültiges Ticket für Kind"));
		}
	}
}
