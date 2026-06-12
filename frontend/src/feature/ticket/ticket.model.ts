export type TicketStatusCategory = "OPEN" | "IN_PROGRESS" | "COMPLETED";

export type TicketPriorityName = "LOWEST" | "LOW" | "NORMAL" | "HIGH" | "HIGHEST";

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
	fromState: string | null;
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
	from: string | null;
	to: string;
};

export type WorkflowState = {
	id: number;
	name: string;
	category: TicketStatusCategory;
};

export type WorkflowTransition = {
	id: number;
	name: string;
	fromStateId: number | null;
	toStateId: number;
};

export type TicketType = {
	id: number;
	name: string;
	description: string;
	states: WorkflowState[];
	transitions: WorkflowTransition[];
	children: number[];
};

export type TicketTypeListResponse = {
	ticketTypes: TicketType[];
};

export type CreateTicketRequest = {
	name: string;
	description: string;
	ticketTypeId: number;
	statusId: number;
	parentTicketId: number | null;
};

export type UpdateTicketStatusRequest = {
	transitionId: number;
};

export type UpdateTicketPriorityRequest = {
	priority: TicketPriorityName;
};

export type Ticket = {
	id: number;
	projectId: number;
	ticketTypeId: number;
	statusId: number;
	priority: TicketPriorityName;
	parentTicketId: number | null;
	name: string;
	description: string;
	createdBy: number;
	assignedTo: number | null;
	createdAt: string | null;
	updatedAt: string | null;
};

export type TicketListResponse = {
	tickets: Ticket[];
};

export type TicketComment = {
	id: number;
	ticketId: number;
	authorId: number;
	authorUsername: string;
	content: string;
	createdAt: string | null;
	updatedAt: string | null;
};

export type TicketCommentListResponse = {
	comments: TicketComment[];
};

export type CreateTicketCommentRequest = {
	content: string;
};
