import { onMounted, ref, watch } from "vue";
import type { DashboardResponse } from "../dashboard.model";
import { useDashboardRepository } from "./useDashboardRepository";

// eslint-disable-next-line @typescript-eslint/explicit-module-boundary-types
export function useProjectDashboard(projectId: number) {
	const repository = useDashboardRepository();
	const data = ref<DashboardResponse | null>(null);
	const isLoading = ref(false);
	const errorMessage = ref("");

	async function load(): Promise<void> {
		isLoading.value = true;
		errorMessage.value = "";

		try {
			data.value = await repository.getProjectDashboard(projectId);
		} catch {
			data.value = null;
			errorMessage.value = "Dashboard konnte nicht geladen werden.";
		} finally {
			isLoading.value = false;
		}
	}

	onMounted(() => {
		load();
	});
	watch(() => projectId, load);

	return { data, isLoading, errorMessage };
}
