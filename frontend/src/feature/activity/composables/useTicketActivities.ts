import { ref, toValue, watch, type MaybeRefOrGetter } from "vue";
import type { ActivityEvent } from "../activity.model";
import { useActivityRepository } from "./useActivityRepository";

// eslint-disable-next-line @typescript-eslint/explicit-module-boundary-types
export function useTicketActivities(
	projectId: MaybeRefOrGetter<number>,
	ticketId: MaybeRefOrGetter<number>,
) {
	const repository = useActivityRepository();
	const events = ref<ActivityEvent[]>([]);
	const isLoading = ref(false);
	const errorMessage = ref("");
	let loadGeneration = 0;

	async function load([currentProjectId, currentTicketId]: readonly [
		number,
		number,
	]): Promise<void> {
		const generation = ++loadGeneration;
		events.value = [];
		isLoading.value = true;
		errorMessage.value = "";

		try {
			const nextEvents = await repository.getTicketActivities(
				currentProjectId,
				currentTicketId,
			);
			if (generation === loadGeneration) {
				events.value = nextEvents;
			}
		} catch {
			if (generation === loadGeneration) {
				errorMessage.value = "Aktivitäten konnten nicht geladen werden.";
			}
		} finally {
			if (generation === loadGeneration) {
				isLoading.value = false;
			}
		}
	}

	watch(() => [toValue(projectId), toValue(ticketId)] as const, load, { immediate: true });

	return { events, isLoading, errorMessage };
}
