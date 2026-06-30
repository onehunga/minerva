import { client } from "@/api";
import type {
	ProjectDetails,
	ProjectRecordListResponse,
	ProjectRole,
	ProjectUserListResponse,
} from "./project.model";

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

export async function getProjectUsers(id: number): Promise<ProjectUserListResponse> {
	return client.get(`/v1/projects/${id}/users`).then((res) => res.data);
}

export async function addProjectUser(
	projectId: number,
	userId: number,
	role: ProjectRole,
): Promise<void> {
	return client.post(`/v1/projects/${projectId}/users`, {
		userId,
		role,
	});
}

export async function updateProjectUserRole(
	projectId: number,
	userId: number,
	role: ProjectRole,
): Promise<void> {
	return client.patch(`/v1/projects/${projectId}/users/${userId}/role`, {
		role,
	});
}
