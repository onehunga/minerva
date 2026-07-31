package de.fallstudie.minerva.backend.ticket;

public sealed interface TicketEvent {
	long actorUserId();

	long projectId();

	long ticketId();

	record TicketCreated(long actorUserId, long projectId, long ticketId, long ticketTypeId,
			long statusId, String name, Long assigneeId) implements TicketEvent {
	}

	record CommentCreated(long actorUserId, long projectId, long ticketId, long commentId,
			String content, Long assigneeId) implements TicketEvent {
	}

	record StatusChanged(long actorUserId, long projectId, long ticketId, long previousStatusId,
			String previousStatusName, long newStatusId, String newStatusName, long transitionId,
			String transitionName, Long assigneeId) implements TicketEvent {
	}

	record DetailsUpdated(long actorUserId, long projectId, long ticketId, String previousName,
			String newName, String previousDescription, String newDescription,
			Long assigneeId) implements TicketEvent {
	}

	record PriorityChanged(long actorUserId, long projectId, long ticketId,
			TicketPriorityName previousPriority, TicketPriorityName newPriority,
			Long assigneeId) implements TicketEvent {
	}

	record AssigneeChanged(long actorUserId, long projectId, long ticketId, Long previousAssigneeId,
			Long newAssigneeId) implements TicketEvent {
	}

	record SubticketAdded(long actorUserId, long projectId, long ticketId, long subticketId,
			String subticketName, Long assigneeId) implements TicketEvent {
	}

	record TicketArchived(long actorUserId, long projectId, long ticketId, String name,
			Long assigneeId) implements TicketEvent {
	}

	record TicketRestored(long actorUserId, long projectId, long ticketId, String name,
			Long assigneeId) implements TicketEvent {
	}

	record TicketDeleted(long actorUserId, long projectId, long ticketId, String name,
			Long assigneeId) implements TicketEvent {
	}
}
