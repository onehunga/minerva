package de.fallstudie.minerva.backend.user;

public sealed interface UserEvent {
	record Deleted(long actorUserId, long userId) implements UserEvent {
	}
}
