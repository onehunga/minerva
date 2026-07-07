package de.fallstudie.minerva.backend.ticket;

public sealed interface TicketEvent
		permits TicketEvent.CommentCreated, TicketEvent.StatusChanged, TicketEvent.AssigneeChanged {
	long actorUserId();

	long projectId();

	long ticketId();

	record CommentCreated(long actorUserId, long projectId, long ticketId, long commentId,
			String content) implements TicketEvent {
	}

	record StatusChanged(long actorUserId, long projectId, long ticketId, long previousStatusId,
			String previousStatusName, long newStatusId, String newStatusName, long transitionId,
			String transitionName) implements TicketEvent {
	}

	record AssigneeChanged(long actorUserId, long projectId, long ticketId, Long previousAssigneeId,
			Long newAssigneeId) implements TicketEvent {
	}
}
