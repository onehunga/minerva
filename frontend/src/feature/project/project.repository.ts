import { api } from ".";
import type { ProjectDetails, ProjectRecord, ProjectRole, ProjectUser } from "./project.model";

export const ProjectRepositoryKey = Symbol("ProjectRepository");

export interface IProjectRepository {
	getAllProjects(): Promise<Array<ProjectRecord>>;
	getProjectDetails(id: number): Promise<ProjectDetails>;
	createProject(name: string, description: string): Promise<number>;
	getProjectUsers(id: number): Promise<Array<ProjectUser>>;
	addProjectUser(projectId: number, userId: number, role: ProjectRole): Promise<void>;
	updateProjectUserRole(projectId: number, userId: number, role: ProjectRole): Promise<void>;
}

export class ProjectRepository implements IProjectRepository {
	async getAllProjects(): Promise<Array<ProjectRecord>> {
		const response = await api.getAllProjects();

		return response.projects;
	}

	async getProjectDetails(id: number): Promise<ProjectDetails> {
		return api.getProjectById(id);
	}

	async createProject(name: string, description: string): Promise<number> {
		return api.createProject(name, description);
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
}
