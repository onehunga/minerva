import { client } from "@/api";
import type { CreateTicketRequest, Ticket, TicketTypeListResponse } from "./ticket.model";

export async function getTicketTypes(projectId: number): Promise<TicketTypeListResponse> {
	return client.get(`/v1/projects/${projectId}/ticket-types`).then((res) => res.data);
}

export async function createTicket(
	projectId: number,
	request: CreateTicketRequest,
): Promise<Ticket> {
	return client.post(`/v1/projects/${projectId}/tickets`, request).then((res) => res.data);
}
