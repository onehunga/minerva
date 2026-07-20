import type { TicketPriorityName, TicketStatusCategory } from "@/feature/ticket/ticket.model";

export type DashboardCategoryCount = {
	open: number;
	inProgress: number;
	completed: number;
};

export type DashboardPriorityRow = {
	priority: TicketPriorityName;
	open: number;
	inProgress: number;
	completed: number;
	total: number;
};

export type DashboardRecentTicket = {
	id: number;
	projectId: number;
	projectName: string | null;
	name: string;
	priority: TicketPriorityName;
	statusName: string;
	statusCategory: TicketStatusCategory;
	createdAt: string | null;
};

export type DashboardResponse = {
	totalTickets: number;
	ticketsByCategory: DashboardCategoryCount;
	priorities: DashboardPriorityRow[];
	recentTickets: DashboardRecentTicket[];
};
