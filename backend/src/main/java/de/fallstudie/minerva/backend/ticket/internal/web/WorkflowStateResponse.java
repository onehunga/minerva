package de.fallstudie.minerva.backend.ticket.internal.web;

import de.fallstudie.minerva.backend.ticket.internal.persistence.WorkflowStatusCategory;

public record WorkflowStateResponse(long id, String name, WorkflowStatusCategory category) {
}
