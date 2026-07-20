import { api } from ".";
import type { ActivityEvent } from "./activity.model";

export const ActivityRepositoryKey = Symbol("ActivityRepository");

export interface IActivityRepository {
	getProjectActivities(projectId: number): Promise<Array<ActivityEvent>>;
	getTicketActivities(projectId: number, ticketId: number): Promise<ActivityEvent[]>;
	getUserActivities(): Promise<ActivityEvent[]>;
	getUserActorActivities(): Promise<ActivityEvent[]>;
}

export class ActivityRepository implements IActivityRepository {
	async getProjectActivities(projectId: number): Promise<Array<ActivityEvent>> {
		const response = await api.getProjectActivities(projectId);

		return response.events;
	}

	async getTicketActivities(projectId: number, ticketId: number): Promise<ActivityEvent[]> {
		const response = await api.getTicketActivities(projectId, ticketId);

		return response.events;
	}

	async getUserActivities(): Promise<ActivityEvent[]> {
		const response = await api.getUserActivities();

		return response.events;
	}

	async getUserActorActivities(): Promise<ActivityEvent[]> {
		const response = await api.getUserActorActivities();

		return response.events;
	}
}
