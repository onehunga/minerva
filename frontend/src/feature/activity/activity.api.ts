import { client } from "@/api";
import type { ActivityEventListResponse } from "./activity.model";

export async function getProjectActivities(projectId: number): Promise<ActivityEventListResponse> {
	return client.get(`/v1/projects/${projectId}/activities`).then((res) => res.data);
}

export async function getTicketActivities(
	projectId: number,
	ticketId: number,
): Promise<ActivityEventListResponse> {
	return client
		.get(`/v1/projects/${projectId}/tickets/${ticketId}/activities`)
		.then((res) => res.data);
}

export async function getUserActivities(): Promise<ActivityEventListResponse> {
	return client.get("/v1/activities").then((res) => res.data);
}

export async function getUserActorActivities(): Promise<ActivityEventListResponse> {
	return client.get("/v1/users/me/activities").then((res) => res.data);
}
