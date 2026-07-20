package de.fallstudie.minerva.backend.activity.internal.service;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.List;

import org.junit.jupiter.api.Test;

import de.fallstudie.minerva.backend.activity.ActivityEventType;
import de.fallstudie.minerva.backend.activity.ActivityScopeType;
import de.fallstudie.minerva.backend.project.ProjectEvent;
import de.fallstudie.minerva.backend.testsupport.TestProjects;

class ProjectActivityListenerTests {
	@Test
	void detailsUpdatedAppendsProjectScopedActivity() {
		final var activityService = mock(ActivityService.class);
		final var listener = new ProjectActivityListener(activityService);
		final var event = new ProjectEvent.DetailsUpdated(TestProjects.OWNER_USER_ID,
				TestProjects.PROJECT_ID, "Minerva", "Minerva 2", "Alt", "Neu");

		listener.on(event);

		verify(activityService).append(ActivityEventType.PROJECT_DETAILS_UPDATED,
				TestProjects.OWNER_USER_ID, event,
				List.of(new ActivityScopeCommand(ActivityScopeType.PROJECT,
						TestProjects.PROJECT_ID)));
	}

	@Test
	void projectArchivedAppendsProjectScopedActivity() {
		final var activityService = mock(ActivityService.class);
		final var listener = new ProjectActivityListener(activityService);
		final var event = new ProjectEvent.ProjectArchived(TestProjects.OWNER_USER_ID,
				TestProjects.PROJECT_ID, "Minerva");

		listener.on(event);

		verify(activityService).append(ActivityEventType.PROJECT_ARCHIVED,
				TestProjects.OWNER_USER_ID, event,
				List.of(new ActivityScopeCommand(ActivityScopeType.PROJECT,
						TestProjects.PROJECT_ID)));
	}
}
