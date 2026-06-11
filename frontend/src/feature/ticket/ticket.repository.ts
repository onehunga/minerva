import { api } from ".";
import type {
	CreateTicketCommentRequest,
	CreateTicketRequest,
	Ticket,
	TicketComment,
	TicketType,
} from "./ticket.model";

export const TicketRepositoryKey = Symbol("TicketRepository");

export interface ITicketRepository {
	getTickets(projectId: number): Promise<Ticket[]>;
	getTicketTypes(projectId: number): Promise<TicketType[]>;
	createTicket(projectId: number, request: CreateTicketRequest): Promise<Ticket>;
	updateTicketStatus(projectId: number, ticketId: number, transitionId: number): Promise<void>;
	getTicketComments(projectId: number, ticketId: number): Promise<TicketComment[]>;
	createTicketComment(
		projectId: number,
		ticketId: number,
		request: CreateTicketCommentRequest,
	): Promise<TicketComment>;
}

export class TicketRepository implements ITicketRepository {
	async getTickets(projectId: number): Promise<Ticket[]> {
		const response = await api.getTickets(projectId);

		return response.tickets;
	}

	async getTicketTypes(projectId: number): Promise<TicketType[]> {
		const response = await api.getTicketTypes(projectId);

		return response.ticketTypes;
	}

	async createTicket(projectId: number, request: CreateTicketRequest): Promise<Ticket> {
		return api.createTicket(projectId, request);
	}

	async updateTicketStatus(
		projectId: number,
		ticketId: number,
		transitionId: number,
	): Promise<void> {
		return api.updateTicketStatus(projectId, ticketId, transitionId);
	}

	async getTicketComments(projectId: number, ticketId: number): Promise<TicketComment[]> {
		const response = await api.getTicketComments(projectId, ticketId);

		return response.comments;
	}

	async createTicketComment(
		projectId: number,
		ticketId: number,
		request: CreateTicketCommentRequest,
	): Promise<TicketComment> {
		return api.createTicketComment(projectId, ticketId, request);
	}
}
