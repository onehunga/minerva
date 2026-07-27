export type TicketStatusCategory = "OPEN" | "IN_PROGRESS" | "COMPLETED";

export type TicketPriorityName = "LOWEST" | "LOW" | "NORMAL" | "HIGH" | "HIGHEST";

export type TicketDetails = {
	name: string;
	description: string;
};

export type WorkflowConfiguration = {
	tickets: TicketWorkflow[];
};

export type TicketWorkflow = {
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

export type UpdateTicketDetailsRequest = TicketDetails;

export type UpdateTicketAssigneeRequest = {
	assignedTo: number | null;
};

export type TicketChild = {
	id: number;
	name: string;
	description: string;
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
	children: TicketChild[];
	createdBy: number;
	assignedTo: number | null;
	createdAt: string | null;
	updatedAt: string | null;
	archived: boolean;
};

export type TicketListResponse = {
	tickets: Ticket[];
};

export type TicketComment = {
	id: number;
	ticketId: number;
	authorId: number;
	authorUsername: string | null;
	authorDeleted: boolean;
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
