import { defineStore } from "pinia";
import { computed, ref } from "vue";
import type { Notification } from "./notification.model";

const mockNotifications: Notification[] = [
	{
		id: 1,
		type: "TICKET_ASSIGNED",
		payload: {
			actorUserId: 42,
			projectId: 7,
			ticketId: 41,
			previousAssigneeId: 42,
			newAssigneeId: 43,
		},
		createdAt: new Date(Date.now() - 5 * 60_000).toISOString(),
		readAt: null,
	},
	{
		id: 2,
		type: "TICKET_COMMENT_CREATED",
		payload: {
			actorUserId: 44,
			projectId: 7,
			ticketId: 41,
			commentId: 12,
			content: "Kannst du dir den neuen Stand bitte ansehen?",
		},
		createdAt: new Date(Date.now() - 45 * 60_000).toISOString(),
		readAt: null,
	},
];

export const useNotificationStore = defineStore("notification", () => {
	const notifications = ref<Notification[]>(mockNotifications);
	const unreadCount = computed(
		() => notifications.value.filter((notification) => notification.readAt === null).length,
	);

	function setNotifications(value: Notification[]): void {
		notifications.value = value;
	}

	function markAsRead(id: number): void {
		const notification = notifications.value.find((item) => item.id === id);
		if (notification?.readAt === null) {
			notification.readAt = new Date().toISOString();
		}
	}

	return {
		notifications,
		unreadCount,
		setNotifications,
		markAsRead,
	};
});
