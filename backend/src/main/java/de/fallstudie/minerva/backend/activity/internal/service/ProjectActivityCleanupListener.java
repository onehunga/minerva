package de.fallstudie.minerva.backend.activity.internal.service;

import de.fallstudie.minerva.backend.project.ProjectDeletedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProjectActivityCleanupListener {
	private final ActivityService activityService;

	@EventListener
	public void on(ProjectDeletedEvent event) {
		log.trace("received ProjectDeletedEvent");

		activityService.deleteProjectActivities(event.projectId());
	}
}
