import { client } from "@/api";
import type { ProjectDetails, ProjectRecordListResponse } from "./project.model";

export async function getAllProjects(): Promise<ProjectRecordListResponse> {
	return client.get("/v1/projects").then((res) => res.data);
}

export async function getProjectById(id: number): Promise<ProjectDetails> {
	return client.get(`/v1/projects/${id}`).then((res) => res.data);
}

export async function createProject(name: string, description: string): Promise<number> {
	return client
		.post("/v1/projects", {
			name,
			description,
		})
		.then((res) => res.data);
}
