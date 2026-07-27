package de.fallstudie.minerva.backend.activity.internal.service;

import de.fallstudie.minerva.backend.activity.ActivityEventType;
import de.fallstudie.minerva.backend.activity.ActivityScopeType;
import de.fallstudie.minerva.backend.user.UserEvent;
import de.fallstudie.minerva.backend.user.WorkspaceRoleName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class UserActivityListenerTests {
	@Test
	void roleChangedAppendsUserScopedActivity() {
		final var activityService = mock(ActivityService.class);
		final var listener = new UserActivityListener(activityService);
		final var event = new UserEvent.WorkspaceRoleChanged(1L, 2L, WorkspaceRoleName.USER,
				WorkspaceRoleName.ADMIN);

		listener.on(event);

		verify(activityService).append(ActivityEventType.USER_WORKSPACE_ROLE_CHANGED, 1L, event,
				List.of(new ActivityScopeCommand(ActivityScopeType.USER, 2L)));
	}
}
