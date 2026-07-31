import { inject } from "vue";
import { ActivityRepositoryKey, type IActivityRepository } from "../activity.repository";

export function useActivityRepository(): IActivityRepository {
	const repo = inject<IActivityRepository>(ActivityRepositoryKey);
	if (repo === undefined) {
		throw new Error("ActivityRepository not provided");
	}

	return repo;
}
