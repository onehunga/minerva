package de.fallstudie.minerva.backend.realtime.internal.stream;

import de.fallstudie.minerva.backend.realtime.UserEventStream;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class UserEventStreamService implements UserEventStream {
	private static final String DATA_INVALIDATED_EVENT = "data.invalidated";

	private final ConcurrentHashMap<Long, Set<SseEmitter>> emittersByUserId = new ConcurrentHashMap<>();

	public SseEmitter connect(long userId) {
		// Zero disables Spring's emitter timeout. Heartbeats below keep intermediary
		// proxies active.
		final var emitter = new SseEmitter(0L);
		emittersByUserId.computeIfAbsent(userId, _ -> ConcurrentHashMap.newKeySet()).add(emitter);
		emitter.onCompletion(() -> unregister(userId, emitter));
		emitter.onTimeout(() -> unregister(userId, emitter));
		emitter.onError(_ -> unregister(userId, emitter));
		send(userId, emitter, "stream.connected", StreamConnectedEvent.now());
		return emitter;
	}

	@Override
	public void publish(long userId, String eventName, Object payload) {
		final var emitters = emittersByUserId.get(userId);
		if (emitters != null) {
			emitters.forEach(emitter -> send(userId, emitter, eventName, payload));
		}
	}

	@Override
	public void broadcast(String eventName, Object payload) {
		emittersByUserId.forEach((userId, emitters) -> emitters
				.forEach(emitter -> send(userId, emitter, eventName, payload)));
	}

	@Override
	public void invalidate(long userId, String... keys) {
		publish(userId, DATA_INVALIDATED_EVENT, new DataInvalidatedEvent(List.of(keys)));
	}

	@Override
	public void invalidate(String... keys) {
		broadcast(DATA_INVALIDATED_EVENT, new DataInvalidatedEvent(List.of(keys)));
	}

	@Scheduled(fixedRate = 25, timeUnit = TimeUnit.SECONDS)
	public void sendHeartbeats() {
		emittersByUserId.forEach((userId, emitters) -> emitters.forEach(
				emitter -> send(userId, emitter, SseEmitter.event().comment("keepalive"))));
	}

	private void send(long userId, SseEmitter emitter, String eventName, Object payload) {
		send(userId, emitter, SseEmitter.event().name(eventName).data(payload));
	}

	private void send(long userId, SseEmitter emitter, SseEmitter.SseEventBuilder event) {
		try {
			emitter.send(event);
		} catch (IOException exception) {
			unregister(userId, emitter);
			emitter.completeWithError(exception);
		}
	}

	private void unregister(long userId, SseEmitter emitter) {
		final var emitters = emittersByUserId.get(userId);
		if (emitters == null) {
			return;
		}
		emitters.remove(emitter);
		if (emitters.isEmpty()) {
			emittersByUserId.remove(userId, emitters);
		}
	}
}
