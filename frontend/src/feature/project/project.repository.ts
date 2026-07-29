import * as api from "./project.api";
import type {
	CreateProjectRequest,
	ProjectDetails,
	ProjectRecord,
	ProjectRole,
	ProjectUser,
	UpdateProjectDetailsRequest,
} from "./project.model";

export const ProjectRepositoryKey = Symbol("ProjectRepository");

export interface IProjectRepository {
	getAllProjects(archived?: boolean): Promise<Array<ProjectRecord>>;
	getAdminProjects(archived?: boolean): Promise<Array<ProjectRecord>>;
	getProjectDetails(id: number): Promise<ProjectDetails>;
	createProject(request: CreateProjectRequest): Promise<number>;
	archiveProject(id: number): Promise<void>;
	restoreProject(id: number): Promise<void>;
	updateProjectDetails(id: number, request: UpdateProjectDetailsRequest): Promise<void>;
	getProjectUsers(id: number): Promise<Array<ProjectUser>>;
	addProjectUser(projectId: number, userId: number, role: ProjectRole): Promise<void>;
	updateProjectUserRole(projectId: number, userId: number, role: ProjectRole): Promise<void>;
	removeProjectUser(projectId: number, userId: number): Promise<void>;
	deleteProject(id: number): Promise<void>;
}

export class ProjectRepository implements IProjectRepository {
	async getAllProjects(archived: boolean = false): Promise<Array<ProjectRecord>> {
		const response = await api.getAllProjects(archived);

		return response.projects;
	}

	async getAdminProjects(archived: boolean = false): Promise<Array<ProjectRecord>> {
		const response = await api.getAdminProjects(archived);

		return response.projects;
	}

	async getProjectDetails(id: number): Promise<ProjectDetails> {
		return api.getProjectById(id);
	}

	async createProject(request: CreateProjectRequest): Promise<number> {
		return api.createProject(request);
	}

	async archiveProject(id: number): Promise<void> {
		return api.archiveProject(id);
	}

	async restoreProject(id: number): Promise<void> {
		return api.restoreProject(id);
	}

	async updateProjectDetails(id: number, request: UpdateProjectDetailsRequest): Promise<void> {
		return api.updateProjectDetails(id, request);
	}

	async getProjectUsers(id: number): Promise<Array<ProjectUser>> {
		const response = await api.getProjectUsers(id);

		return response.users;
	}

	async addProjectUser(projectId: number, userId: number, role: ProjectRole): Promise<void> {
		return api.addProjectUser(projectId, userId, role);
	}

	async updateProjectUserRole(
		projectId: number,
		userId: number,
		role: ProjectRole,
	): Promise<void> {
		return api.updateProjectUserRole(projectId, userId, role);
	}

	async removeProjectUser(projectId: number, userId: number): Promise<void> {
		return api.removeProjectUser(projectId, userId);
	}

	async deleteProject(id: number): Promise<void> {
		return api.deleteProject(id);
	}
}
