import { inject } from "vue";
import {
	NotificationRepositoryKey,
	type INotificationRepository,
} from "../notification.repository";

export function useNotificationRepository(): INotificationRepository {
	const repository = inject<INotificationRepository>(NotificationRepositoryKey);
	if (repository === undefined) {
		throw new Error("NotificationRepository not provided");
	}
	return repository;
}
