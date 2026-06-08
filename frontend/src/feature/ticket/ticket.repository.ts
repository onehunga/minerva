import { api } from ".";
import type { CreateTicketRequest, Ticket, TicketType } from "./ticket.model";

export const TicketRepositoryKey = Symbol("TicketRepository");

export interface ITicketRepository {
	getTickets(projectId: number): Promise<Ticket[]>;
	getTicketTypes(projectId: number): Promise<TicketType[]>;
	createTicket(projectId: number, request: CreateTicketRequest): Promise<Ticket>;
	updateTicketStatus(projectId: number, ticketId: number, transitionId: number): Promise<void>;
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
}
