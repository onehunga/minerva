package de.fallstudie.minerva.backend.activity.internal.web;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import de.fallstudie.minerva.backend.activity.internal.service.ActivityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/v1/projects/{projectId}")
@RequiredArgsConstructor
public class ActivityController {
	private final ActivityService activityService;

	@GetMapping("/tickets/{ticketId}/activities")
	@PreAuthorize("@projectPolicy.canViewProject(principal, #projectId)")
	public ActivityEventListResponse getTicketActivities(@PathVariable long projectId,
			@PathVariable long ticketId) {
		log.info("Requesting activities for ticket with ID {} in project with ID {}", ticketId,
				projectId);

		return activityService.getTicketActivities(projectId, ticketId);
	}
}
