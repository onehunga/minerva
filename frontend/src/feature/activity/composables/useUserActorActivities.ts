import { onMounted, ref } from "vue";
import type { ActivityEvent } from "../activity.model";
import { useActivityRepository } from "./useActivityRepository";

// eslint-disable-next-line @typescript-eslint/explicit-module-boundary-types
export function useUserActorActivities() {
	const repository = useActivityRepository();
	const events = ref<ActivityEvent[]>([]);
	const isLoading = ref(false);
	const errorMessage = ref("");

	async function load(): Promise<void> {
		isLoading.value = true;
		errorMessage.value = "";

		try {
			events.value = await repository.getUserActorActivities();
		} catch {
			events.value = [];
			errorMessage.value = "Eigene Aktivitäten konnten nicht geladen werden.";
		} finally {
			isLoading.value = false;
		}
	}

	onMounted(() => {
		load();
	});

	return { events, isLoading, errorMessage };
}
