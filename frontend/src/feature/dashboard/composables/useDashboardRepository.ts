import { inject } from "vue";
import { DashboardRepositoryKey, type IDashboardRepository } from "../dashboard.repository";

export function useDashboardRepository(): IDashboardRepository {
	const repo = inject<IDashboardRepository>(DashboardRepositoryKey);
	if (repo === undefined) {
		throw new Error("DashboardRepository not provided");
	}

	return repo;
}
