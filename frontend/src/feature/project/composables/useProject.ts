import { storeToRefs } from "pinia";
import { toValue, watch, type MaybeRefOrGetter } from "vue";
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
export function useProject(projectId?: MaybeRefOrGetter<string>) {
	const store = useActiveProjectStore();
	const projectRepository = useProjectRepository();
	const ticketRepository = useTicketRepository();

	if (projectId !== undefined) {
		watch(
			() => toValue(projectId),
			(currentProjectId) => {
				if (currentProjectId === "" || isNaN(Number(currentProjectId))) {
					throw new Error("Invalid project ID");
				}

				if (store.activeProject === currentProjectId) {
					return;
				}

				store.setActiveProject(currentProjectId);
				store.setProjectDetails(null);
				store.setProjectUsers([]);
				store.setTicketTypes([]);
				store.setTickets([]);
				void fetchProjectData(currentProjectId);
			},
			{ immediate: true },
		);
	} else if (!store.activeProject) {
		throw new Error("Invalid State: No project ID provided and no active project set.");
	}

	async function fetchTickets(archived: boolean = false): Promise<void> {
		const tickets = await ticketRepository.getTickets(Number(store.activeProject), archived);
		store.setTickets(tickets, archived);
	}

	async function fetchProjectData(projectId: string): Promise<void> {
		const numericProjectId = Number(projectId);
		const [details, projectUsers, ticketTypes, tickets] = await Promise.all([
			projectRepository.getProjectDetails(numericProjectId),
			projectRepository.getProjectUsers(numericProjectId),
			ticketRepository.getTicketTypes(numericProjectId),
			ticketRepository.getTickets(numericProjectId),
		]);

		store.setProjectDetails(details);
		store.setProjectUsers(projectUsers);
		store.setTicketTypes(ticketTypes);
		store.setTickets(tickets);
	}

	async function createTicket(req: CreateTicketRequest): Promise<Ticket> {
		const newTicket = await ticketRepository.createTicket(Number(store.activeProject), req);
		store.addTicket(newTicket);
		return newTicket;
	}

	async function updateProjectDetails(name: string, description: string): Promise<void> {
		await projectRepository.updateProjectDetails(Number(store.activeProject), {
			name,
			description,
		});
		store.updateProjectDetails(name, description);
	}

	async function deleteTicket(ticketId: number): Promise<void> {
		await ticketRepository.deleteTicket(Number(store.activeProject), ticketId);
		store.removeTicket(ticketId);
	}

	async function archiveTicket(ticketId: number): Promise<void> {
		await ticketRepository.archiveTicket(Number(store.activeProject), ticketId);
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

	async function updateTicketDetails(
		ticketId: number,
		name: string,
		description: string,
	): Promise<void> {
		await ticketRepository.updateTicketDetails(Number(store.activeProject), ticketId, {
			name,
			description,
		});

		store.updateTicketDetails(ticketId, name, description);
	}

	async function updateTicketAssignee(
		ticketId: number,
		assignedTo: number | null,
	): Promise<void> {
		await ticketRepository.updateTicketAssignee(Number(store.activeProject), ticketId, {
			assignedTo,
		});

		store.updateTicketAssignee(ticketId, assignedTo);
	}

	const { details, projectUsers, ticketTypes, tickets } = storeToRefs(store);

	return {
		details,
		projectUsers,
		ticketTypes,
		tickets,
		fetchTickets,
		createTicket,
		updateProjectDetails,
		deleteTicket,
		archiveTicket,
		updateTicketStatus,
		updateTicketPriority,
		updateTicketDetails,
		updateTicketAssignee,
	};
}
