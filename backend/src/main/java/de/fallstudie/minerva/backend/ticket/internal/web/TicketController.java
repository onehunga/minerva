package de.fallstudie.minerva.backend.ticket.internal.web;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import de.fallstudie.minerva.backend.ticket.internal.service.TicketService;
import de.fallstudie.minerva.backend.user.Identity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/v1/projects/{projectId}")
@RequiredArgsConstructor
public class TicketController {
	private final TicketService ticketService;

	@GetMapping("/ticket-types")
	@PreAuthorize("@projectPolicy.canViewProject(principal, #projectId)")
	public TicketTypeListResponse getTicketTypes(@PathVariable long projectId) {
		log.info("Requesting ticket types for project with ID {}", projectId);

		return ticketService.getTicketTypes(projectId);
	}

	@PostMapping("/tickets")
	@ResponseStatus(HttpStatus.CREATED)
	@PreAuthorize("@projectPolicy.canCreateTickets(principal, #projectId)")
	public TicketResponse createTicket(@AuthenticationPrincipal Identity identity,
			@PathVariable long projectId, @RequestBody CreateTicketRequest request) {
		log.info("User with ID {} is creating a ticket in project with ID {}", identity.userId(),
				projectId);

		return ticketService.createTicket(identity, projectId, request);
	}
}
