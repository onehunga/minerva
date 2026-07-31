import * as api from "./dashboard.api";
import type { DashboardResponse } from "./dashboard.model";

export const DashboardRepositoryKey = Symbol("DashboardRepository");

export interface IDashboardRepository {
	getProjectDashboard(projectId: number): Promise<DashboardResponse>;
	getGlobalDashboard(): Promise<DashboardResponse>;
}

export class DashboardRepository implements IDashboardRepository {
	async getProjectDashboard(projectId: number): Promise<DashboardResponse> {
		return api.getProjectDashboard(projectId);
	}

	async getGlobalDashboard(): Promise<DashboardResponse> {
		return api.getGlobalDashboard();
	}
}
