import { ref, toValue, watch, type MaybeRefOrGetter } from "vue";
import type { ActivityEvent } from "../activity.model";
import { useActivityRepository } from "./useActivityRepository";

// eslint-disable-next-line @typescript-eslint/explicit-module-boundary-types
export function useProjectActivities(projectId: MaybeRefOrGetter<number>) {
	const repository = useActivityRepository();
	const events = ref<ActivityEvent[]>([]);
	const isLoading = ref(false);
	const errorMessage = ref("");
	let loadGeneration = 0;

	async function load(currentProjectId: number): Promise<void> {
		const generation = ++loadGeneration;
		events.value = [];
		isLoading.value = true;
		errorMessage.value = "";

		try {
			const nextEvents = await repository.getProjectActivities(currentProjectId);
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

	watch(() => toValue(projectId), load, { immediate: true });

	return { events, isLoading, errorMessage };
}
