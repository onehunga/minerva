package de.fallstudie.minerva.backend.ticket.internal.web;

import de.fallstudie.minerva.backend.ticket.TicketPriorityName;

public record UpdateTicketPriorityRequest(TicketPriorityName priority) {
}
