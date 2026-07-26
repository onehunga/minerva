import { ref, toValue, watch, type MaybeRefOrGetter } from "vue";
import type { DashboardResponse } from "../dashboard.model";
import { useDashboardRepository } from "./useDashboardRepository";

// eslint-disable-next-line @typescript-eslint/explicit-module-boundary-types
export function useProjectDashboard(projectId: MaybeRefOrGetter<number>) {
	const repository = useDashboardRepository();
	const data = ref<DashboardResponse | null>(null);
	const isLoading = ref(false);
	const errorMessage = ref("");

	async function load(currentProjectId: number): Promise<void> {
		data.value = null;
		isLoading.value = true;
		errorMessage.value = "";

		try {
			data.value = await repository.getProjectDashboard(currentProjectId);
		} catch {
			errorMessage.value = "Dashboard konnte nicht geladen werden.";
		} finally {
			isLoading.value = false;
		}
	}

	watch(() => toValue(projectId), load, { immediate: true });

	return { data, isLoading, errorMessage };
}
