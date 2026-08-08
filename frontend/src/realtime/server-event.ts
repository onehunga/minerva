export type StreamConnectedEvent = {
	type: "stream.connected";
	data: {
		connectedAt: string;
	};
};

export type DataInvalidatedEvent = {
	type: "data.invalidated";
	data: {
		keys: string[];
	};
};

export type ServerEvent = StreamConnectedEvent | DataInvalidatedEvent;

function isRecord(value: unknown): value is Record<string, unknown> {
	return typeof value === "object" && value !== null;
}

/**
 * Keeps the protocol boundary narrow: malformed or future server events do not reach stores.
 */
export function parseServerEvent(type: string, data: string): ServerEvent | null {
	try {
		const value: unknown = JSON.parse(data);

		if (
			type === "stream.connected" &&
			isRecord(value) &&
			typeof value.connectedAt === "string"
		) {
			return { type, data: { connectedAt: value.connectedAt } };
		}

		if (
			type === "data.invalidated" &&
			isRecord(value) &&
			Array.isArray(value.keys) &&
			value.keys.every((key: unknown): key is string => typeof key === "string")
		) {
			return { type, data: { keys: value.keys } };
		}
	} catch {
		// A broken event must not terminate the long-lived connection.
	}

	return null;
}
