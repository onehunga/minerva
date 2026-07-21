package de.fallstudie.minerva.backend.activity.internal.service;

import de.fallstudie.minerva.backend.activity.ActivityEventType;
import de.fallstudie.minerva.backend.activity.ActivityScopeType;
import de.fallstudie.minerva.backend.project.ProjectEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ProjectActivityListener {
	private final ActivityService activityService;

	@EventListener
	public void on(ProjectEvent event) {
		var activity = switch (event) {
			case ProjectEvent.ProjectCreated _ -> ActivityEventType.PROJECT_CREATED;
			case ProjectEvent.DetailsUpdated _ -> ActivityEventType.PROJECT_DETAILS_UPDATED;
			case ProjectEvent.ProjectArchived _ -> ActivityEventType.PROJECT_ARCHIVED;
			case ProjectEvent.UserAdded _ -> ActivityEventType.PROJECT_USER_ADDED;
			case ProjectEvent.UserRoleChanged _ -> ActivityEventType.PROJECT_USER_ROLE_CHANGED;
			case ProjectEvent.UserRemoved _ -> ActivityEventType.PROJECT_USER_REMOVED;
		};

		var scopes = List
				.of(new ActivityScopeCommand(ActivityScopeType.PROJECT, event.projectId()));

		activityService.append(activity, event.actorUserId(), event, scopes);
	}
}
