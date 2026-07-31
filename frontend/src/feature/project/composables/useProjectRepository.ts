import { inject } from "vue";
import { ProjectRepositoryKey, type IProjectRepository } from "../project.repository";

export function useProjectRepository(): IProjectRepository {
	const repo = inject<IProjectRepository>(ProjectRepositoryKey);
	if (repo === undefined) {
		throw new Error("UserRepository not provided");
	}

	return repo!;
}
