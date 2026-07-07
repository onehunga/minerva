package de.fallstudie.minerva.backend.activity.internal.service;

import de.fallstudie.minerva.backend.activity.ActivityEventType;
import de.fallstudie.minerva.backend.activity.ActivityScopeType;
import de.fallstudie.minerva.backend.ticket.TicketEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class TicketActivityListener {
	private final ActivityService activityService;

	@EventListener
	public void on(TicketEvent event) {
		var activity = switch (event) {
			case TicketEvent.CommentCreated _ -> ActivityEventType.TICKET_COMMENT_CREATED;
			case TicketEvent.StatusChanged _ -> ActivityEventType.TICKET_STATUS_CHANGED;
			case TicketEvent.AssigneeChanged _ -> ActivityEventType.TICKET_ASSIGNEE_CHANGED;
		};

		var scopes = List.of(new ActivityScopeCommand(ActivityScopeType.PROJECT, event.projectId()),
				new ActivityScopeCommand(ActivityScopeType.TICKET, event.ticketId()));

		activityService.append(activity, event, scopes);
	}
}
