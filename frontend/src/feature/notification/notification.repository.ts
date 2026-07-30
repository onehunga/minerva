import * as api from "./notification.api";
import type { Notification } from "./notification.model";

export const NotificationRepositoryKey = Symbol("NotificationRepository");

export interface INotificationRepository {
	getAll(): Promise<Notification[]>;
	markAsRead(notificationId: number): Promise<void>;
}

export class NotificationRepository implements INotificationRepository {
	async getAll(): Promise<Notification[]> {
		return (await api.getNotifications()).notifications;
	}

	async markAsRead(notificationId: number): Promise<void> {
		await api.markNotificationAsRead(notificationId);
	}
}
