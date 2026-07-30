package de.fallstudie.minerva.backend.notification.internal;

public sealed interface Notification {
	long recipientUserId();

	record TicketAssigned(long recipientUserId, long actorUserId, long projectId, long ticketId,
			Long previousAssigneeId, Long newAssigneeId) implements Notification {
	}

	record TicketUnassigned(long recipientUserId, long actorUserId, long projectId, long ticketId,
			Long previousAssigneeId, Long newAssigneeId) implements Notification {
	}

	record TicketCommentCreated(long recipientUserId, long actorUserId, long projectId,
			long ticketId, long commentId, String content) implements Notification {
	}

	record TicketStatusChanged(long recipientUserId, long actorUserId, long projectId,
			long ticketId, long previousStatusId, String previousStatusName, long newStatusId,
			String newStatusName, long transitionId,
			String transitionName) implements Notification {
	}

	record TicketDetailsUpdated(long recipientUserId, long actorUserId, long projectId,
			long ticketId, String previousName, String newName, String previousDescription,
			String newDescription) implements Notification {
	}

	record TicketPriorityChanged(long recipientUserId, long actorUserId, long projectId,
			long ticketId, String previousPriority, String newPriority) implements Notification {
	}

	record TicketSubticketAdded(long recipientUserId, long actorUserId, long projectId,
			long ticketId, long subticketId, String subticketName) implements Notification {
	}

	record TicketArchived(long recipientUserId, long actorUserId, long projectId, long ticketId,
			String name) implements Notification {
	}

	record TicketRestored(long recipientUserId, long actorUserId, long projectId, long ticketId,
			String name) implements Notification {
	}

	record TicketDeleted(long recipientUserId, long actorUserId, long projectId, long ticketId,
			String name) implements Notification {
	}

	record ProjectUserAdded(long recipientUserId, long actorUserId, long projectId,
			String role) implements Notification {
	}

	record ProjectUserRoleChanged(long recipientUserId, long actorUserId, long projectId,
			String oldRole, String newRole) implements Notification {
	}

	record ProjectUserRemoved(long recipientUserId, long actorUserId,
			long projectId) implements Notification {
	}

	record UserCreated(long recipientUserId, long actorUserId, String username,
			String role) implements Notification {
	}

	record UserUsernameChanged(long recipientUserId, long actorUserId, String previousUsername,
			String newUsername) implements Notification {
	}

	record UserPasswordChanged(long recipientUserId, long actorUserId) implements Notification {
	}

	record UserWorkspaceRoleChanged(long recipientUserId, long actorUserId, String oldRole,
			String newRole) implements Notification {
	}

	record UserDeactivated(long recipientUserId, long actorUserId) implements Notification {
	}

	record UserReactivated(long recipientUserId, long actorUserId) implements Notification {
	}
}
