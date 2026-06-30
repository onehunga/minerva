package de.fallstudie.minerva.backend.ticket.internal.web;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import de.fallstudie.minerva.backend.ticket.TicketConfigurationService;
import de.fallstudie.minerva.backend.ticket.WorkflowConfigurationsCreateRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/v1/projects/{projectId}/ticket-configurations")
@RequiredArgsConstructor
public class TicketConfigurationController {
	private final TicketConfigurationService ticketConfigurationService;

	@PostMapping("")
	@ResponseStatus(HttpStatus.CREATED)
	@PreAuthorize("@projectPolicy.canModifyTickets(principal, #projectId)")
	public void createTicketConfiguration(@PathVariable long projectId,
			@RequestBody WorkflowConfigurationsCreateRequest request) {
		log.info("Creating ticket configuration for project with ID {}", projectId);

		ticketConfigurationService.createTicketConfiguration(projectId, request);
	}
}
