import { client } from "@/api";
import type {
	CreateProjectRequest,
	ProjectDetails,
	ProjectRecordListResponse,
	ProjectRole,
	ProjectUserListResponse,
	UpdateProjectDetailsRequest,
} from "./project.model";

export async function getAllProjects(): Promise<ProjectRecordListResponse> {
	return client.get("/v1/projects").then((res) => res.data);
}

export async function getProjectById(id: number): Promise<ProjectDetails> {
	return client.get(`/v1/projects/${id}`).then((res) => res.data);
}

export async function createProject(request: CreateProjectRequest): Promise<number> {
	return client.post("/v1/projects", request).then((res) => res.data);
}

export async function archiveProject(id: number): Promise<void> {
	return client.patch(`/v1/projects/${id}/archive`);
}

export async function updateProjectDetails(
	id: number,
	request: UpdateProjectDetailsRequest,
): Promise<void> {
	return client.patch(`/v1/projects/${id}/details`, request);
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

export async function removeProjectUser(projectId: number, userId: number): Promise<void> {
	return client.delete(`/v1/projects/${projectId}/users/${userId}`);
}
