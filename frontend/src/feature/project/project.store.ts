import { defineStore } from "pinia";
import { ref } from "vue";
import type { ProjectDetails, ProjectRecord, ProjectUser } from "./project.model";
import type { Ticket, TicketPriorityName, TicketType } from "../ticket/ticket.model";

export const useActiveProjectStore = defineStore("active.project", () => {
	const activeProject = ref<null | string>(null);
	const details = ref<null | ProjectDetails>(null);
	const projectUsers = ref<ProjectUser[]>([]);
	const ticketTypes = ref<TicketType[]>([]);
	const tickets = ref<Ticket[]>([]);
	const ticketsArchived = ref(false);

	function setActiveProject(project: string | null): void {
		activeProject.value = project;
	}

	function setProjectDetails(projectDetails: ProjectDetails | null): void {
		details.value = projectDetails;
	}

	function updateProjectDetails(name: string, description: string): void {
		if (details.value != null) {
			details.value.name = name;
			details.value.description = description;
		}
	}

	function setProjectUsers(users: ProjectUser[]): void {
		projectUsers.value = users;
	}

	function setTicketTypes(projectTicketTypes: TicketType[]): void {
		ticketTypes.value = projectTicketTypes;
	}

	function setTickets(projectTickets: Ticket[], archived: boolean = false): void {
		tickets.value = projectTickets;
		ticketsArchived.value = archived;
	}

	function addTicket(ticket: Ticket): void {
		if (!ticketsArchived.value) {
			tickets.value.push(ticket);
		}
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

	function updateTicketDetails(ticketId: number, name: string, description: string): void {
		const ticket = tickets.value.find((currentTicket) => currentTicket.id === ticketId);

		if (ticket !== undefined) {
			ticket.name = name;
			ticket.description = description;
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
		updateProjectDetails,
		setProjectUsers,
		setTicketTypes,
		setTickets,
		addTicket,
		removeTicket,
		updateTicketStatus,
		updateTicketPriority,
		updateTicketDetails,
		updateTicketAssignee,
	};
});

export const useProjectsStore = defineStore("projects", () => {
	const projects = ref<ProjectRecord[]>([]);

	function setProjects(projectRecords: ProjectRecord[]): void {
		projects.value = projectRecords;
	}

	return {
		projects,
		setProjects,
	};
});
