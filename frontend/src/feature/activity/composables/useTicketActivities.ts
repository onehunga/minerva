import { onMounted, ref, toValue, watch } from "vue";
import type { ActivityEvent } from "../activity.model";
import { useActivityRepository } from "./useActivityRepository";

// eslint-disable-next-line @typescript-eslint/explicit-module-boundary-types
export function useTicketActivities(projectId: number, ticketId: number) {
	const repository = useActivityRepository();
	const events = ref<ActivityEvent[]>([]);
	const isLoading = ref(false);
	const errorMessage = ref("");

	async function load(): Promise<void> {
		isLoading.value = true;
		errorMessage.value = "";

		try {
			events.value = await repository.getTicketActivities(
				toValue(projectId),
				toValue(ticketId),
			);
		} catch {
			events.value = [];
			errorMessage.value = "Aktivitäten konnten nicht geladen werden.";
		} finally {
			isLoading.value = false;
		}
	}

	onMounted(() => {
		load();
	});

	watch(
		() => [toValue(projectId), toValue(ticketId)],
		() => {
			load();
		},
	);

	return { events, isLoading, errorMessage };
}
