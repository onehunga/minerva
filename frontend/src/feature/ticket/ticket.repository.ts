import { api } from ".";
import type { CreateTicketRequest, Ticket, TicketType } from "./ticket.model";

export const TicketRepositoryKey = Symbol("TicketRepository");

export interface ITicketRepository {
	getTicketTypes(projectId: number): Promise<TicketType[]>;
	createTicket(projectId: number, request: CreateTicketRequest): Promise<Ticket>;
}

export class TicketRepository implements ITicketRepository {
	async getTicketTypes(projectId: number): Promise<TicketType[]> {
		const response = await api.getTicketTypes(projectId);

		return response.ticketTypes;
	}

	async createTicket(projectId: number, request: CreateTicketRequest): Promise<Ticket> {
		return api.createTicket(projectId, request);
	}
}
