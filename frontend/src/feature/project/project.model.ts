import type { WorkflowConfiguration } from "@/feature/ticket/ticket.model";

export type CreateProjectRequest = {
	name: string;
	description: string;
	ticketConfiguration: WorkflowConfiguration;
};

export type ProjectRecord = {
	id: number;
	name: string;
	description: string;
	projectRole?: ProjectRole | null;
	openTicketCount?: number;
	ticketCount?: number;
};

export type ProjectRecordListResponse = {
	projects: ProjectRecord[];
};

export type ProjectRole = "OWNER" | "CONTRIBUTOR" | "VIEWER";

export type ProjectDetails = {
	id: number;
	name: string;
	description: string;
	projectRole: ProjectRole | null;
	archived: boolean;
};

export type UpdateProjectDetailsRequest = Pick<ProjectDetails, "name" | "description">;

export type ProjectUser = {
	id: number;
	username: string;
	projectRole: ProjectRole | null;
};

export type ProjectUserListResponse = {
	users: ProjectUser[];
};
