import { client } from "@/api";
import type { DashboardResponse } from "./dashboard.model";

export async function getProjectDashboard(projectId: number): Promise<DashboardResponse> {
	return client.get(`/v1/projects/${projectId}/dashboard`).then((res) => res.data);
}

export async function getGlobalDashboard(): Promise<DashboardResponse> {
	return client.get(`/v1/dashboard`).then((res) => res.data);
}
