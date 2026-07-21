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
			case TicketEvent.TicketCreated _ -> ActivityEventType.TICKET_CREATED;
			case TicketEvent.CommentCreated _ -> ActivityEventType.TICKET_COMMENT_CREATED;
			case TicketEvent.StatusChanged _ -> ActivityEventType.TICKET_STATUS_CHANGED;
			case TicketEvent.DetailsUpdated _ -> ActivityEventType.TICKET_DETAILS_UPDATED;
			case TicketEvent.PriorityChanged _ -> ActivityEventType.TICKET_PRIORITY_CHANGED;
			case TicketEvent.AssigneeChanged _ -> ActivityEventType.TICKET_ASSIGNEE_CHANGED;
			case TicketEvent.SubticketAdded _ -> ActivityEventType.TICKET_SUBTICKET_ADDED;
			case TicketEvent.TicketArchived _ -> ActivityEventType.TICKET_ARCHIVED;
			case TicketEvent.TicketDeleted _ -> ActivityEventType.TICKET_DELETED;
		};

		var scopes = event instanceof TicketEvent.TicketDeleted
				? List.of(new ActivityScopeCommand(ActivityScopeType.PROJECT, event.projectId()))
				: List.of(new ActivityScopeCommand(ActivityScopeType.PROJECT, event.projectId()),
						new ActivityScopeCommand(ActivityScopeType.TICKET, event.ticketId()));

		activityService.append(activity, event.actorUserId(), event, scopes);
	}
}
