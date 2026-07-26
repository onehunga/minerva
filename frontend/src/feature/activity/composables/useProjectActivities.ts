import { ref, toValue, watch, type MaybeRefOrGetter } from "vue";
import type { ActivityEvent } from "../activity.model";
import { useActivityRepository } from "./useActivityRepository";

// eslint-disable-next-line @typescript-eslint/explicit-module-boundary-types
export function useProjectActivities(projectId: MaybeRefOrGetter<number>) {
	const repository = useActivityRepository();
	const events = ref<ActivityEvent[]>([]);
	const isLoading = ref(false);
	const errorMessage = ref("");

	async function load(currentProjectId: number): Promise<void> {
		events.value = [];
		isLoading.value = true;
		errorMessage.value = "";

		try {
			events.value = await repository.getProjectActivities(currentProjectId);
		} catch {
			errorMessage.value = "Aktivitäten konnten nicht geladen werden.";
		} finally {
			isLoading.value = false;
		}
	}

	watch(() => toValue(projectId), load, { immediate: true });

	return { events, isLoading, errorMessage };
}
