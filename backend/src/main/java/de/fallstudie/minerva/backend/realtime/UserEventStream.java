package de.fallstudie.minerva.backend.realtime;

/// Cross-module API of the application-wide SSE stream. Modules send events to
/// connected clients without managing the transport themselves.
///
/// Invalidation keys are hierarchical, e.g. `projects`, `projects:42:users` or `notifications`.
/// Clients refetch the data behind a key through the regular REST endpoints.
public interface UserEventStream {
	void publish(long userId, String eventName, Object payload);

	void broadcast(String eventName, Object payload);

	/**
	 * Tells a single user that the datasets behind the given keys are stale.
	 */
	void invalidate(long userId, String... keys);

	/**
	 * Tells every connected user that the datasets behind the given keys are stale.
	 */
	void invalidate(String... keys);
}
