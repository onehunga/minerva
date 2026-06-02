import { api } from ".";
import type { ProjectDetails, ProjectRecord } from "./project.model";

export const ProjectRepositoryKey = Symbol("ProjectRepository");

export interface IProjectRepository {
	getAllProjects(): Promise<Array<ProjectRecord>>;
	getProjectDetails(id: number): Promise<ProjectDetails>;
	createProject(name: string, description: string): Promise<number>;
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
}
