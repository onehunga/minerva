import { defineStore } from "pinia";
import { computed, ref } from "vue";
import type { Notification } from "./notification.model";

export const useNotificationStore = defineStore("notification", () => {
	const notifications = ref<Notification[]>([]);
	const isLoading = ref(false);
	const errorMessage = ref("");
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

	function setLoading(value: boolean): void {
		isLoading.value = value;
	}

	function setErrorMessage(value: string): void {
		errorMessage.value = value;
	}

	return {
		notifications,
		unreadCount,
		isLoading,
		errorMessage,
		setNotifications,
		markAsRead,
		setLoading,
		setErrorMessage,
	};
});
