package de.fallstudie.minerva.backend.realtime.internal.web;

import de.fallstudie.minerva.backend.realtime.internal.stream.UserEventStreamService;
import de.fallstudie.minerva.backend.user.Identity;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/v1/events")
@RequiredArgsConstructor
public class EventStreamController {
	private final UserEventStreamService userEventStreamService;

	@GetMapping(produces = MediaType.TEXT_EVENT_STREAM_VALUE)
	public SseEmitter connect(@AuthenticationPrincipal Identity identity) {
		return userEventStreamService.connect(identity.userId());
	}
}
