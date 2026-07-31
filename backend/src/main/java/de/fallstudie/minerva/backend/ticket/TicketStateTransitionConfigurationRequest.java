package de.fallstudie.minerva.backend.ticket;

public record TicketStateTransitionConfigurationRequest(String name, String from, String to) {
}
