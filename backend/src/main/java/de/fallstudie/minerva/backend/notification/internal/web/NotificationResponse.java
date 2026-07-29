package de.fallstudie.minerva.backend.notification.internal.web;

import de.fallstudie.minerva.backend.notification.internal.NotificationType;
import tools.jackson.databind.JsonNode;

import java.time.Instant;

public record NotificationResponse(long id, NotificationType type, JsonNode payload,
		Instant createdAt, Instant readAt) {
}
