export type TicketStatusCategory = "OPEN" | "IN_PROGRESS" | "CLOSED";

export type TicketDetails = {
	name: string;
	description: string;
};

export type CreateTicketType = TicketDetails & {
	states: Map<string, CreateWorkflowState>;
	transitions: CreateTicketTransition[];
	children: Set<string>;
};

export type CreateWorkflowState = {
	name: string;
	statusCategory: TicketStatusCategory;
};

export type CreateTicketTransition = {
	name: string;
	fromState: string;
	toState: string;
};
