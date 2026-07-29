package de.fallstudie.minerva.backend.notification.internal.web;

import de.fallstudie.minerva.backend.notification.internal.service.NotificationService;
import de.fallstudie.minerva.backend.user.Identity;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {
	private final NotificationService notificationService;

	@GetMapping
	public NotificationListResponse getAll(@AuthenticationPrincipal Identity identity) {
		return notificationService.getAll(identity.userId());
	}

	@PatchMapping("/{notificationId}/read")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void markAsRead(@AuthenticationPrincipal Identity identity,
			@PathVariable long notificationId) {
		notificationService.markAsRead(identity.userId(), notificationId);
	}
}
