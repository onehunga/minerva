package de.fallstudie.minerva.backend.ticket.internal.web;

public record WorkflowTransitionResponse(long id, String name, Long fromStateId, long toStateId) {
}
