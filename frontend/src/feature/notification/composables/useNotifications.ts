import { storeToRefs } from "pinia";
import type { ComputedRef, Ref } from "vue";
import { useNotificationStore } from "../notification.store";
import type { Notification } from "../notification.model";

type UseNotificationsResult = {
	notifications: Ref<Notification[]>;
	unreadCount: ComputedRef<number>;
	markAsRead: (id: number) => void;
};

export function useNotifications(): UseNotificationsResult {
	const store = useNotificationStore();
	const { notifications, unreadCount } = storeToRefs(store);

	return { notifications, unreadCount, markAsRead: store.markAsRead };
}
