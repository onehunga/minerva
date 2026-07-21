package de.fallstudie.minerva.backend.activity.internal.web;

import java.time.Instant;

import de.fallstudie.minerva.backend.activity.ActivityEventType;
import tools.jackson.databind.JsonNode;

public record ActivityEventResponse(long id, ActivityEventType type, int schemaVersion,
		Long actorUserId, String actorUsername, boolean actorDeleted, Instant occurredAt,
		JsonNode payload) {
}
