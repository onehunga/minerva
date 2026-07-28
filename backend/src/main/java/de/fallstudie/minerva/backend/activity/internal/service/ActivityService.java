package de.fallstudie.minerva.backend.activity.internal.service;

import de.fallstudie.minerva.backend.activity.ActivityEventType;
import de.fallstudie.minerva.backend.activity.ActivityScopeType;
import de.fallstudie.minerva.backend.activity.internal.persistence.ActivityEventModel;
import de.fallstudie.minerva.backend.activity.internal.persistence.ActivityEventRepository;
import de.fallstudie.minerva.backend.activity.internal.persistence.ActivityScopeModel;
import de.fallstudie.minerva.backend.activity.internal.persistence.ActivityScopeRepository;
import de.fallstudie.minerva.backend.activity.internal.web.ActivityEventListResponse;
import de.fallstudie.minerva.backend.activity.internal.web.ActivityEventResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ActivityService {
	private static final int SCHEMA_VERSION = 1;

	private final ActivityEventRepository activityEventRepository;
	private final ActivityScopeRepository activityScopeRepository;
	private final ObjectMapper objectMapper;

	@Transactional
	public void append(ActivityEventType type, long actorUserId, Object payload,
			List<ActivityScopeCommand> scopes) {
		assert type != null;
		assert payload != null;
		assert scopes != null && !scopes.isEmpty();

		final var occurredAt = Instant.now();
		final var model = new ActivityEventModel();
		model.setType(type);
		model.setSchemaVersion(SCHEMA_VERSION);
		model.setActorUserId(actorUserId);
		model.setPayloadJson(toJson(payload));
		model.setOccurredAt(occurredAt);

		final var savedEvent = activityEventRepository.save(model);
		final var scopeModels = scopes.stream()
				.map(scope -> toScopeModel(savedEvent.getId(), scope, occurredAt)).toList();
		activityScopeRepository.saveAll(scopeModels);
	}

	public ActivityEventListResponse getProjectActivities(long projectId) {
		final var events = activityEventRepository
				.findAllForProject(ActivityScopeType.PROJECT, projectId).stream()
				.map(this::toResponse).toList();

		return new ActivityEventListResponse(events);
	}

	public ActivityEventListResponse getTicketActivities(long projectId, long ticketId) {
		final var events = activityEventRepository
				.findAllForTicketInProject(ActivityScopeType.PROJECT, projectId,
						ActivityScopeType.TICKET, ticketId)
				.stream().map(this::toResponse).toList();

		return new ActivityEventListResponse(events);
	}

	public ActivityEventListResponse getUserActivities(long userId) {
		final var events = activityEventRepository.findAllForUser(userId).stream()
				.map(this::toResponse).toList();

		return new ActivityEventListResponse(events);
	}

	public ActivityEventListResponse getUserActorActivities(long userId) {
		final var events = activityEventRepository.findAllForUserActor(userId).stream()
				.map(this::toResponse).toList();

		return new ActivityEventListResponse(events);
	}

	private ActivityScopeModel toScopeModel(long eventId, ActivityScopeCommand scope,
			Instant occurredAt) {
		final var model = new ActivityScopeModel();
		model.setEventId(eventId);
		model.setScopeType(scope.type());
		model.setScopeId(scope.id());
		model.setOccurredAt(occurredAt);
		return model;
	}

	private ActivityEventResponse toResponse(ActivityEventModel event) {
		return new ActivityEventResponse(event.getId(), event.getType(), event.getSchemaVersion(),
				event.getActorUserId(), event.getOccurredAt(), toJsonNode(event.getPayloadJson()));
	}

	private String toJson(Object payload) {
		try {
			return objectMapper.writeValueAsString(payload);
		} catch (JacksonException exception) {
			throw new IllegalStateException("Could not serialize activity payload", exception);
		}
	}

	private JsonNode toJsonNode(String payloadJson) {
		try {
			return objectMapper.readTree(payloadJson);
		} catch (JacksonException exception) {
			throw new IllegalStateException("Could not deserialize activity payload", exception);
		}
	}
}
