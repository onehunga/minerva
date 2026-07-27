package de.fallstudie.minerva.backend.activity.internal.service;

import de.fallstudie.minerva.backend.activity.ActivityEventType;
import de.fallstudie.minerva.backend.activity.ActivityScopeType;
import de.fallstudie.minerva.backend.user.UserEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class UserActivityListener {
	private final ActivityService activityService;

	@EventListener
	public void on(UserEvent event) {
		final var activity = switch (event) {
			case UserEvent.Created _ -> ActivityEventType.USER_CREATED;
			case UserEvent.UsernameChanged _ -> ActivityEventType.USER_USERNAME_CHANGED;
			case UserEvent.PasswordChanged _ -> ActivityEventType.USER_PASSWORD_CHANGED;
			case UserEvent.WorkspaceRoleChanged _ -> ActivityEventType.USER_WORKSPACE_ROLE_CHANGED;
			case UserEvent.Deleted _ -> ActivityEventType.USER_DELETED;
		};

		final var scopes = List
				.of(new ActivityScopeCommand(ActivityScopeType.USER, event.userId()));
		activityService.append(activity, event.actorUserId(), event, scopes);
	}
}
