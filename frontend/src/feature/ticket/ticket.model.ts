export type TicketStatusCategory = "OPEN" | "IN_PROGRESS" | "COMPLETED";

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

export type WorkflowConfiguration = {
	tickets: CreateTicketTypeRequest[];
};

export type CreateTicketTypeRequest = {
	name: string;
	description: string;
	states: CreateWorkflowStateRequest[];
	transitions: CreateWorkflowTransitionRequest[];
	children: string[];
};

export type CreateWorkflowStateRequest = {
	name: string;
	category: TicketStatusCategory;
};

export type CreateWorkflowTransitionRequest = {
	name: string;
	from: string;
	to: string;
};
