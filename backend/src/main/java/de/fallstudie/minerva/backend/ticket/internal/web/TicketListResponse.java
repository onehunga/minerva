package de.fallstudie.minerva.backend.ticket.internal.web;

import java.util.List;

public record TicketListResponse(List<TicketResponse> tickets) {
}
