export type ProjectRecord = {
	id: number;
	name: string;
};

export type ProjectRecordListResponse = {
	projects: ProjectRecord[];
};

export type ProjectRole = "OWNER" | "CONTRIBUTOR" | "VIEWER";

export type ProjectDetails = {
	id: number;
	name: string;
	description: string;
	projectRole: ProjectRole;
};

export type ProjectUser = {
	id: number;
	username: string;
	projectRole: ProjectRole | null;
	member: boolean;
};

export type ProjectUserListResponse = {
	users: ProjectUser[];
};
