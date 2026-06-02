export type ProjectRecord = {
	id: number;
	name: string;
};

export type ProjectRecordListResponse = {
	projects: ProjectRecord[];
};

export type ProjectDetails = {
	id: number;
	name: string;
	description: string;
};
