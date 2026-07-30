import { client } from "@/api";
import type { NotificationListResponse } from "./notification.model";

export async function getNotifications(): Promise<NotificationListResponse> {
	return client.get("/v1/notifications").then((response) => response.data);
}

export async function markNotificationAsRead(notificationId: number): Promise<void> {
	await client.patch(`/v1/notifications/${notificationId}/read`);
}
