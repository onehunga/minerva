import * as api from "./ticket.api";
import type {
	CreateTicketCommentRequest,
	CreateTicketRequest,
	Ticket,
	TicketComment,
	TicketPriorityName,
	TicketType,
	UpdateTicketAssigneeRequest,
	UpdateTicketDetailsRequest,
} from "./ticket.model";

export const TicketRepositoryKey = Symbol("TicketRepository");

export interface ITicketRepository {
	getTickets(projectId: number, archived?: boolean): Promise<Ticket[]>;
	getTicketTypes(projectId: number): Promise<TicketType[]>;
	createTicket(projectId: number, request: CreateTicketRequest): Promise<Ticket>;
	deleteTicket(projectId: number, ticketId: number): Promise<void>;
	archiveTicket(projectId: number, ticketId: number): Promise<void>;
	restoreTicket(projectId: number, ticketId: number): Promise<void>;
	updateTicketStatus(projectId: number, ticketId: number, transitionId: number): Promise<void>;
	updateTicketPriority(
		projectId: number,
		ticketId: number,
		priority: TicketPriorityName,
	): Promise<void>;
	updateTicketDetails(
		projectId: number,
		ticketId: number,
		request: UpdateTicketDetailsRequest,
	): Promise<void>;
	updateTicketAssignee(
		projectId: number,
		ticketId: number,
		request: UpdateTicketAssigneeRequest,
	): Promise<void>;
	getTicketComments(projectId: number, ticketId: number): Promise<TicketComment[]>;
	createTicketComment(
		projectId: number,
		ticketId: number,
		request: CreateTicketCommentRequest,
	): Promise<TicketComment>;
}

export class TicketRepository implements ITicketRepository {
	async getTickets(projectId: number, archived: boolean = false): Promise<Ticket[]> {
		const response = await api.getTickets(projectId, archived);

		return response.tickets;
	}

	async getTicketTypes(projectId: number): Promise<TicketType[]> {
		const response = await api.getTicketTypes(projectId);

		return response.ticketTypes;
	}

	async createTicket(projectId: number, request: CreateTicketRequest): Promise<Ticket> {
		return api.createTicket(projectId, request);
	}

	async deleteTicket(projectId: number, ticketId: number): Promise<void> {
		return api.deleteTicket(projectId, ticketId);
	}

	async archiveTicket(projectId: number, ticketId: number): Promise<void> {
		return api.archiveTicket(projectId, ticketId);
	}

	async restoreTicket(projectId: number, ticketId: number): Promise<void> {
		return api.restoreTicket(projectId, ticketId);
	}

	async updateTicketStatus(
		projectId: number,
		ticketId: number,
		transitionId: number,
	): Promise<void> {
		return api.updateTicketStatus(projectId, ticketId, transitionId);
	}

	async updateTicketPriority(
		projectId: number,
		ticketId: number,
		priority: TicketPriorityName,
	): Promise<void> {
		return api.updateTicketPriority(projectId, ticketId, priority);
	}

	async updateTicketDetails(
		projectId: number,
		ticketId: number,
		request: UpdateTicketDetailsRequest,
	): Promise<void> {
		return api.updateTicketDetails(projectId, ticketId, request);
	}

	async updateTicketAssignee(
		projectId: number,
		ticketId: number,
		request: UpdateTicketAssigneeRequest,
	): Promise<void> {
		return api.updateTicketAssignee(projectId, ticketId, request);
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
