import { fetchEventSource, type EventSourceMessage } from "@microsoft/fetch-event-source";
import { getAuthorizationHeader, isAuthenticated, refreshTokens } from "@/api";
import { watch } from "vue";
import { parseServerEvent } from "./server-event";

let initialized = false;
let abortController: AbortController | null = null;

export function initializeRealtime(): void {
	if (initialized) {
		return;
	}
	initialized = true;

	watch(
		isAuthenticated,
		(authenticated: boolean): void => {
			if (authenticated) {
				startEventStream();
			} else {
				stopEventStream();
			}
		},
		{ immediate: true },
	);
}

function startEventStream(): void {
	if (abortController !== null) {
		return;
	}

	abortController = new AbortController();
	const signal: AbortSignal = abortController.signal;

	fetchEventSource("/api/v1/events", {
		signal,
		async fetch(input: RequestInfo | URL, init?: RequestInit): Promise<Response> {
			const authorizationHeader: string | null = getAuthorizationHeader();
			if (authorizationHeader === null) {
				throw new StreamAuthenticationError();
			}

			const response = await window.fetch(input, {
				...init,
				headers: createStreamHeaders(init?.headers, authorizationHeader),
			});

			if (response.status !== 401) {
				return response;
			}

			try {
				await refreshTokens();
			} catch {
				throw new StreamAuthenticationError();
			}
			const refreshedAuthorizationHeader: string | null = getAuthorizationHeader();
			if (refreshedAuthorizationHeader === null) {
				throw new StreamAuthenticationError();
			}

			return window.fetch(input, {
				...init,
				headers: createStreamHeaders(init?.headers, refreshedAuthorizationHeader),
			});
		},
		async onopen(response: Response): Promise<void> {
			if (response.status === 401) {
				throw new StreamAuthenticationError();
			}
			if (!response.ok) {
				throw new Error(`Event stream failed with status ${response.status}.`);
			}
			if (!response.headers.get("content-type")?.startsWith("text/event-stream")) {
				throw new Error("Event stream did not return text/event-stream.");
			}
		},
		onmessage(message: EventSourceMessage): void {
			const event = parseServerEvent(message.event, message.data);
			if (event !== null) {
				// This is deliberately visible while SSE is introduced and documents each received event.
				console.info("[SSE]", event.type, event.data);
			}
		},
		onclose(): void {
			throw new Error("Event stream closed.");
		},
		onerror(error: unknown): void {
			if (error instanceof StreamAuthenticationError) {
				stopEventStream();
				throw error;
			}

			if (!signal.aborted) {
				console.warn("[SSE] reconnecting after an event-stream error.", error);
			}
		},
	})
		.catch((error: unknown): void => {
			if (!signal.aborted) {
				console.error("[SSE] event stream stopped.", error);
			}
		})
		.finally((): void => {
			if (abortController?.signal === signal) {
				abortController = null;
			}
		});
}

function stopEventStream(): void {
	abortController?.abort();
	abortController = null;
}

function createStreamHeaders(
	headers: HeadersInit | undefined,
	authorizationHeader: string,
): Headers {
	const streamHeaders = new Headers(headers);
	streamHeaders.set("Authorization", authorizationHeader);
	return streamHeaders;
}

class StreamAuthenticationError extends Error {}
