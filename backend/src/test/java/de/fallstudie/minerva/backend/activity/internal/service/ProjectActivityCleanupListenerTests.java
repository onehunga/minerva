package de.fallstudie.minerva.backend.activity.internal.service;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;

import de.fallstudie.minerva.backend.project.ProjectDeletedEvent;
import de.fallstudie.minerva.backend.testsupport.TestProjects;

class ProjectActivityCleanupListenerTests {
	@Test
	void deletesProjectActivitiesOnProjectDeletedEvent() {
		final var activityService = mock(ActivityService.class);
		final var listener = new ProjectActivityCleanupListener(activityService);

		listener.on(new ProjectDeletedEvent(TestProjects.PROJECT_ID));

		verify(activityService).deleteProjectActivities(TestProjects.PROJECT_ID);
	}
}
