package de.fallstudie.minerva.backend.activity.internal.web;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import de.fallstudie.minerva.backend.activity.internal.service.ActivityService;
import de.fallstudie.minerva.backend.user.Identity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/v1")
@RequiredArgsConstructor
public class UserActivityController {
	private final ActivityService activityService;

	@GetMapping("/activities")
	public ActivityEventListResponse getUserActivities(@AuthenticationPrincipal Identity identity) {
		log.info("User with ID {} is requesting all accessible activities", identity.userId());

		return activityService.getUserActivities(identity.userId());
	}

	@GetMapping("/users/me/activities")
	public ActivityEventListResponse getUserActorActivities(
			@AuthenticationPrincipal Identity identity) {
		log.info("User with ID {} is requesting their own activities", identity.userId());

		return activityService.getUserActorActivities(identity.userId());
	}
}
