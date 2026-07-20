package de.fallstudie.minerva.backend.ticket.internal.web;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import de.fallstudie.minerva.backend.ticket.internal.service.TicketCommentService;
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
	private final TicketCommentService ticketCommentService;

	@GetMapping("/tickets")
	@PreAuthorize("@projectPolicy.canViewProject(principal, #projectId)")
	public TicketListResponse getTickets(@PathVariable long projectId) {
		log.info("Requesting tickets for project with ID {}", projectId);

		return ticketService.getTickets(projectId);
	}

	@GetMapping("/ticket-types")
	@PreAuthorize("@projectPolicy.canViewProject(principal, #projectId)")
	public TicketTypeListResponse getTicketTypes(@PathVariable long projectId) {
		log.info("Requesting ticket types for project with ID {}", projectId);

		return ticketService.getTicketTypes(projectId);
	}

	@PostMapping("/tickets")
	@ResponseStatus(HttpStatus.CREATED)
	@PreAuthorize("@projectPolicy.canModifyTickets(principal, #projectId)")
	public TicketResponse createTicket(@AuthenticationPrincipal Identity identity,
			@PathVariable long projectId, @RequestBody CreateTicketRequest request) {
		log.info("User with ID {} is creating a ticket in project with ID {}", identity.userId(),
				projectId);

		return ticketService.createTicket(identity, projectId, request);
	}

	@DeleteMapping("/tickets/{ticketId}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@PreAuthorize("@projectPolicy.canModifyTickets(principal, #projectId)")
	public void deleteTicket(@PathVariable long projectId, @PathVariable long ticketId) {
		log.info("Deleting ticket with ID {} in project with ID {}", ticketId, projectId);

		ticketService.deleteTicket(projectId, ticketId);
	}

	@GetMapping("/tickets/{ticketId}/comments")
	@PreAuthorize("@projectPolicy.canViewProject(principal, #projectId)")
	public TicketCommentListResponse getTicketComments(@PathVariable long projectId,
			@PathVariable long ticketId) {
		log.info("Requesting comments for ticket with ID {} in project with ID {}", ticketId,
				projectId);

		return ticketCommentService.getTicketComments(projectId, ticketId);
	}

	@PostMapping("/tickets/{ticketId}/comments")
	@ResponseStatus(HttpStatus.CREATED)
	@PreAuthorize("@projectPolicy.canModifyTickets(principal, #projectId)")
	public TicketCommentResponse createTicketComment(@AuthenticationPrincipal Identity identity,
			@PathVariable long projectId, @PathVariable long ticketId,
			@RequestBody CreateTicketCommentRequest request) {
		log.info(
				"User with ID {} is creating a comment for ticket with ID {} in project with ID {}",
				identity.userId(), ticketId, projectId);

		return ticketCommentService.createTicketComment(identity, projectId, ticketId, request);
	}

	@PatchMapping("/tickets/{ticketId}/status")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@PreAuthorize("@projectPolicy.canModifyTickets(principal, #projectId)")
	public void updateTicketStatus(@AuthenticationPrincipal Identity identity,
			@PathVariable long projectId, @PathVariable long ticketId,
			@RequestBody UpdateTicketStatusRequest request) {
		log.info("Updating status of ticket with ID {} in project with ID {}", ticketId, projectId);

		ticketService.updateTicketStatus(identity, projectId, ticketId, request);
	}

	@PatchMapping("/tickets/{ticketId}/details")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@PreAuthorize("@projectPolicy.canModifyTickets(principal, #projectId)")
	public void updateTicketDetails(@AuthenticationPrincipal Identity identity,
			@PathVariable long projectId, @PathVariable long ticketId,
			@RequestBody UpdateTicketDetailsRequest request) {
		log.info("Updating details of ticket with ID {} in project with ID {}", ticketId,
				projectId);

		ticketService.updateTicketDetails(identity, projectId, ticketId, request);
	}

	@PatchMapping("/tickets/{ticketId}/priority")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@PreAuthorize("@projectPolicy.canModifyTickets(principal, #projectId)")
	public void updateTicketPriority(@AuthenticationPrincipal Identity identity,
			@PathVariable long projectId, @PathVariable long ticketId,
			@RequestBody UpdateTicketPriorityRequest request) {
		log.info("Updating priority of ticket with ID {} in project with ID {}", ticketId,
				projectId);

		ticketService.updateTicketPriority(identity, projectId, ticketId, request);
	}

	@PatchMapping("/tickets/{ticketId}/assignee")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@PreAuthorize("@projectPolicy.canModifyTickets(principal, #projectId)")
	public void updateTicketAssignee(@AuthenticationPrincipal Identity identity,
			@PathVariable long projectId, @PathVariable long ticketId,
			@RequestBody UpdateTicketAssigneeRequest request) {
		log.info("Updating assignee of ticket with ID {} in project with ID {}", ticketId,
				projectId);

		ticketService.updateTicketAssignee(identity, projectId, ticketId, request);
	}
}
