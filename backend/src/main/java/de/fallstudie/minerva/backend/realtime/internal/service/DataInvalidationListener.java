package de.fallstudie.minerva.backend.realtime.internal.service;

import de.fallstudie.minerva.backend.project.ProjectEvent;
import de.fallstudie.minerva.backend.realtime.UserEventStream;
import de.fallstudie.minerva.backend.ticket.TicketEvent;
import de.fallstudie.minerva.backend.user.UserEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Translates committed domain events into broadcast invalidations, so feature
 * modules get live client updates without depending on the realtime module.
 */
@Component
@RequiredArgsConstructor
public class DataInvalidationListener {
	private final UserEventStream userEventStream;

	@Async
	@TransactionalEventListener
	public void on(TicketEvent event) {
		userEventStream.invalidate("projects:%d:tickets".formatted(event.projectId()));
	}

	@Async
	@TransactionalEventListener
	public void on(ProjectEvent event) {
		final var key = switch (event) {
			case ProjectEvent.UserAdded _,ProjectEvent.UserRoleChanged _,ProjectEvent.UserRemoved _ ->
				"projects:%d:users".formatted(event.projectId());
			case ProjectEvent.ProjectCreated _,ProjectEvent.DetailsUpdated _,ProjectEvent.ProjectArchived _,ProjectEvent.ProjectRestored _ ->
				"projects";
		};
		userEventStream.invalidate(key);
	}

	@Async
	@TransactionalEventListener
	public void on(UserEvent event) {
		userEventStream.invalidate("users");
	}
}
