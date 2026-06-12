import { storeToRefs } from "pinia";
import { useTicketRepository } from "@/feature/ticket";
import { useActiveProjectStore, useProjectRepository } from "..";
import type { CreateTicketRequest, Ticket, TicketPriorityName } from "@/feature/ticket";

/**
 * Management für das aktive Projekt.
 * Stellt sicher, dass eine gültige Projekt-ID vorhanden ist und lädt die Projektdetails, wenn sich das aktive Projekt ändert.
 *
 * @param projectId
 * @returns
 */
// eslint-disable-next-line @typescript-eslint/explicit-module-boundary-types
export function useProject(projectId?: string) {
	const store = useActiveProjectStore();
	const projectRepository = useProjectRepository();
	const ticketRepository = useTicketRepository();

	if (projectId) {
		if (isNaN(Number(projectId))) {
			throw new Error("Invalid project ID");
		}

		if (store.activeProject != projectId) {
			store.setActiveProject(projectId);

			fetchProjectData();
		}
	} else if (!store.activeProject) {
		throw new Error("Invalid State: No project ID provided and no active project set.");
	}

	async function fetchProjectDetails() {
		store.setProjectDetails(
			await projectRepository.getProjectDetails(Number(store.activeProject)),
		);
	}

	async function fetchTicketTypes() {
		store.setTicketTypes(await ticketRepository.getTicketTypes(Number(store.activeProject)));
	}

	async function fetchTickets() {
		store.setTickets(await ticketRepository.getTickets(Number(store.activeProject)));
	}

	async function fetchProjectData() {
		await Promise.all([fetchProjectDetails(), fetchTicketTypes(), fetchTickets()]);
	}

	async function createTicket(req: CreateTicketRequest): Promise<Ticket> {
		const newTicket = await ticketRepository.createTicket(Number(store.activeProject), req);
		store.addTicket(newTicket);
		return newTicket;
	}

	async function deleteTicket(ticketId: number): Promise<void> {
		await ticketRepository.deleteTicket(Number(store.activeProject), ticketId);
		store.removeTicket(ticketId);
	}

	async function updateTicketStatus(
		ticketId: number,
		transitionId: number,
		statusId: number,
	): Promise<void> {
		await ticketRepository.updateTicketStatus(
			Number(store.activeProject),
			ticketId,
			transitionId,
		);

		store.updateTicketStatus(ticketId, statusId);
	}

	async function updateTicketPriority(
		ticketId: number,
		priority: TicketPriorityName,
	): Promise<void> {
		await ticketRepository.updateTicketPriority(
			Number(store.activeProject),
			ticketId,
			priority,
		);

		store.updateTicketPriority(ticketId, priority);
	}

	const { details, ticketTypes, tickets } = storeToRefs(store);

	return {
		details,
		ticketTypes,
		tickets,
		createTicket,
		deleteTicket,
		updateTicketStatus,
		updateTicketPriority,
	};
}
