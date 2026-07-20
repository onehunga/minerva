package de.fallstudie.minerva.backend.project;

public sealed interface ProjectEvent {
	long actorUserId();

	long projectId();

	record ProjectCreated(long actorUserId, long projectId, String name,
			String description) implements ProjectEvent {
	}

	record DetailsUpdated(long actorUserId, long projectId, String previousName, String newName,
			String previousDescription, String newDescription) implements ProjectEvent {
	}

	record UserAdded(long actorUserId, long projectId, long userId,
			String role) implements ProjectEvent {
	}

	record UserRoleChanged(long actorUserId, long projectId, long userId, String oldRole,
			String newRole) implements ProjectEvent {
	}

	record UserRemoved(long actorUserId, long projectId, long userId) implements ProjectEvent {
	}
}
