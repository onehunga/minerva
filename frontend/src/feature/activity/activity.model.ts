export type ActivityEventType =
	| "PROJECT_CREATED"
	| "PROJECT_DETAILS_UPDATED"
	| "PROJECT_ARCHIVED"
	| "PROJECT_USER_ADDED"
	| "PROJECT_USER_ROLE_CHANGED"
	| "PROJECT_USER_REMOVED"
	| "TICKET_CREATED"
	| "TICKET_COMMENT_CREATED"
	| "TICKET_STATUS_CHANGED"
	| "TICKET_DETAILS_UPDATED"
	| "TICKET_PRIORITY_CHANGED"
	| "TICKET_ASSIGNEE_CHANGED"
	| "TICKET_SUBTICKET_ADDED"
	| "TICKET_ARCHIVED"
	| "TICKET_DELETED";

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
