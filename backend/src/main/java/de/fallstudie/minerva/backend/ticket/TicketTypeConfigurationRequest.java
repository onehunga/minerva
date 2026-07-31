package de.fallstudie.minerva.backend.ticket;

import java.util.List;

public record TicketTypeConfigurationRequest(String name, String description,
		List<TicketStateConfigurationRequest> states,
		List<TicketStateTransitionConfigurationRequest> transitions, List<String> children) {
}
