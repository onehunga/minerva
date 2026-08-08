package de.fallstudie.minerva.backend.realtime.internal.stream;

import java.time.Instant;

public record StreamConnectedEvent(Instant connectedAt) {
	public static StreamConnectedEvent now() {
		return new StreamConnectedEvent(Instant.now());
	}
}
