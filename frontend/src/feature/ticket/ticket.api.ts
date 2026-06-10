import { client } from "@/api";
import type {
	CreateTicketCommentRequest,
	CreateTicketRequest,
	Ticket,
	TicketComment,
	TicketCommentListResponse,
	TicketListResponse,
	TicketTypeListResponse,
} from "./ticket.model";

export async function getTickets(projectId: number): Promise<TicketListResponse> {
	return client.get(`/v1/projects/${projectId}/tickets`).then((res) => res.data);
}

export async function getTicketTypes(projectId: number): Promise<TicketTypeListResponse> {
	return client.get(`/v1/projects/${projectId}/ticket-types`).then((res) => res.data);
}

export async function createTicket(
	projectId: number,
	request: CreateTicketRequest,
): Promise<Ticket> {
	return client.post(`/v1/projects/${projectId}/tickets`, request).then((res) => res.data);
}

export async function updateTicketStatus(
	projectId: number,
	ticketId: number,
	transitionId: number,
): Promise<void> {
	return client.patch(`/v1/projects/${projectId}/tickets/${ticketId}/status`, { transitionId });
}

export async function getTicketComments(
	projectId: number,
	ticketId: number,
): Promise<TicketCommentListResponse> {
	return client
		.get(`/v1/projects/${projectId}/tickets/${ticketId}/comments`)
		.then((res) => res.data);
}

export async function createTicketComment(
	projectId: number,
	ticketId: number,
	request: CreateTicketCommentRequest,
): Promise<TicketComment> {
	return client
		.post(`/v1/projects/${projectId}/tickets/${ticketId}/comments`, request)
		.then((res) => res.data);
}
