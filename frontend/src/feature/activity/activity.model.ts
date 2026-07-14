export type ActivityEventType =
	| "TICKET_CREATED"
	| "TICKET_COMMENT_CREATED"
	| "TICKET_STATUS_CHANGED"
	| "TICKET_DETAILS_UPDATED"
	| "TICKET_PRIORITY_CHANGED"
	| "TICKET_ASSIGNEE_CHANGED"
	| "TICKET_SUBTICKET_ADDED";

export type ActivityEvent = {
	id: number;
	type: ActivityEventType;
	schemaVersion: number;
	actorUserId: number | null;
	actorUsername: string | null;
	occurredAt: string;
	payload: Record<string, unknown>;
};

export type ActivityEventListResponse = {
	events: ActivityEvent[];
};
