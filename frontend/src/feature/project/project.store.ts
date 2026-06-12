import { defineStore } from "pinia";
import { ref } from "vue";
import type { model } from ".";
import type { Ticket, TicketPriorityName, TicketType } from "../ticket/ticket.model";

export const useActiveProjectStore = defineStore("activeUsers", () => {
	const activeProject = ref<null | string>(null);
	const details = ref<null | model.ProjectDetails>(null);
	const ticketTypes = ref<TicketType[]>([]);
	const tickets = ref<Ticket[]>([]);

	function setActiveProject(project: string | null): void {
		activeProject.value = project;
	}

	function setProjectDetails(projectDetails: model.ProjectDetails | null): void {
		details.value = projectDetails;
	}

	function setTicketTypes(projectTicketTypes: TicketType[]): void {
		ticketTypes.value = projectTicketTypes;
	}

	function setTickets(projectTickets: Ticket[]): void {
		tickets.value = projectTickets;
	}

	function addTicket(ticket: Ticket): void {
		tickets.value.push(ticket);
	}

	function updateTicketStatus(ticketId: number, statusId: number): void {
		const ticket = tickets.value.find((currentTicket) => currentTicket.id === ticketId);

		if (ticket !== undefined) {
			ticket.statusId = statusId;
			ticket.updatedAt = new Date().toISOString();
		}
	}

	function updateTicketPriority(ticketId: number, priority: TicketPriorityName): void {
		const ticket = tickets.value.find((currentTicket) => currentTicket.id === ticketId);

		if (ticket !== undefined) {
			ticket.priority = priority;
			ticket.updatedAt = new Date().toISOString();
		}
	}

	return {
		activeProject,
		details,
		ticketTypes,
		tickets,
		setActiveProject,
		setProjectDetails,
		setTicketTypes,
		setTickets,
		addTicket,
		updateTicketStatus,
		updateTicketPriority,
	};
});
