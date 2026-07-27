package de.fallstudie.minerva.backend.user;

public sealed interface UserEvent {
	long actorUserId();

	long userId();

	record Created(long actorUserId, long userId, String username,
			WorkspaceRoleName role) implements UserEvent {
	}

	record UsernameChanged(long actorUserId, long userId, String previousUsername,
			String newUsername) implements UserEvent {
	}

	record PasswordChanged(long actorUserId, long userId) implements UserEvent {
	}

	record WorkspaceRoleChanged(long actorUserId, long userId, WorkspaceRoleName oldRole,
			WorkspaceRoleName newRole) implements UserEvent {
	}

	record Deleted(long actorUserId, long userId, String username) implements UserEvent {
		public Deleted(long actorUserId, long userId) {
			this(actorUserId, userId, null);
		}
	}
}
