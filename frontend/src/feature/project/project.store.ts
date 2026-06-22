import { defineStore } from "pinia";
import { ref } from "vue";
import type { model } from ".";
import type { Ticket, TicketPriorityName, TicketType } from "../ticket/ticket.model";

export const useActiveProjectStore = defineStore("activeUsers", () => {
	const activeProject = ref<null | string>(null);
	const details = ref<null | model.ProjectDetails>(null);
	const projectUsers = ref<model.ProjectUser[]>([]);
	const ticketTypes = ref<TicketType[]>([]);
	const tickets = ref<Ticket[]>([]);

	function setActiveProject(project: string | null): void {
		activeProject.value = project;
	}

	function setProjectDetails(projectDetails: model.ProjectDetails | null): void {
		details.value = projectDetails;
	}

	function setProjectUsers(users: model.ProjectUser[]): void {
		projectUsers.value = users;
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

	function removeTicket(ticketId: number): void {
		tickets.value = tickets.value.filter((ticket) => ticket.id !== ticketId);
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

	function updateTicketAssignee(ticketId: number, assignedTo: number | null): void {
		const ticket = tickets.value.find((currentTicket) => currentTicket.id === ticketId);

		if (ticket !== undefined) {
			ticket.assignedTo = assignedTo;
			ticket.updatedAt = new Date().toISOString();
		}
	}

	return {
		activeProject,
		details,
		projectUsers,
		ticketTypes,
		tickets,
		setActiveProject,
		setProjectDetails,
		setProjectUsers,
		setTicketTypes,
		setTickets,
		addTicket,
		removeTicket,
		updateTicketStatus,
		updateTicketPriority,
		updateTicketAssignee,
	};
});
