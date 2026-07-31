import { storeToRefs } from "pinia";
import { useNotificationStore } from "../notification.store";
import { useNotificationRepository } from "./useNotificationRepository";
import { onMounted } from "vue";

// eslint-disable-next-line @typescript-eslint/explicit-module-boundary-types
export function useNotifications() {
	const store = useNotificationStore();
	const repository = useNotificationRepository();
	const { notifications, unreadCount, isLoading, errorMessage } = storeToRefs(store);

	async function load(): Promise<void> {
		store.setNotifications([]);
		store.setErrorMessage("");
		store.setLoading(true);
		try {
			store.setNotifications(await repository.getAll());
		} catch {
			store.setErrorMessage("Benachrichtigungen konnten nicht geladen werden.");
		} finally {
			store.setLoading(false);
		}
	}

	async function markAsRead(id: number): Promise<void> {
		const notification = notifications.value.find((item) => item.id === id);
		if (notification === undefined || notification.readAt !== null) {
			return;
		}
		await repository.markAsRead(id);
		store.markAsRead(id);
	}

	onMounted(async () => {
		await load();
	});

	return { notifications, unreadCount, isLoading, errorMessage, load, markAsRead };
}
