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

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
// TODO: wollen wir validierung serverseitig haben und als komplette liste ans
// FE geben?
public class WorkflowConfigurationsValidator {
	private final WorkflowConfigurationsCreateRequest req;
	private TicketTypeConfigurationRequest current = null;

	public void validate() {
		req.tickets().forEach(this::validateTicketConfiguration);
	}

	private void validateTicketConfiguration(TicketTypeConfigurationRequest ticket) {
		this.current = ticket;

		log.trace("called validate for {}", ticket.name());
		if (ticket.name() == null || ticket.name().isBlank()) {
			log.error("ticket type name is empty");
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
			log.error("No Ticket state");
			throw new IllegalArgumentException("Ticket states must not be empty");
		}

		final var stateSet = states.stream().map(TicketStateConfigurationRequest::name)
				.collect(Collectors.toSet());

		if (states.size() != stateSet.size()) {
			log.error("No unique ticket states");

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
		final var seenTransitions = new HashSet<String>();

		for (final var transition : transitions) {
			assert transition.name() != null : "Transitions must not be null";
			assert transition.to() != null : "Transition to state must not be null";

			if (transition.from() != null && !stateNames.contains(transition.from())) {
				log.error("transition {} from state {} is invalid", transition.name(),
						transition.from());

				throw new ValidationException(
						"Übergangszustand " + transition.from() + " ist kein gültiger Zustand");
			}
			if (!stateNames.contains(transition.to())) {
				log.error("transition {} to state {} is invalid", transition.name(),
						transition.to());

				throw new ValidationException(
						"Übergangszustand " + transition.to() + " ist kein gültiger Zustand");
			}
			if (Objects.equals(transition.from(), transition.to())) {
				log.error("transition {} cant have the same from and to state", transition.name());

				throw new ValidationException(
						"Übergang von und zu demselben Zustand ist nicht erlaubt");
			}

			final var transitionKey = transition.from() + "->" + transition.to();
			if (!seenTransitions.add(transitionKey)) {
				log.error("transition key: {} exists already for {}", transitionKey,
						current.name());

				throw new DuplicateResourceException(
						"Übergang " + transitionKey + " ist bereits definiert");
			}
		}
	}

	private void validateChildren(List<String> children) {
		for (final var child : children) {
			req.tickets().stream().filter(t -> t.name().equals(child)).findFirst()
					.orElseThrow(() -> {
						log.error("{} is not a valid child ticket", child);

						return new ValidationException(
								"Ticket " + child + " ist kein Gültiges Ticket für Kind");
					});
		}
	}
}
