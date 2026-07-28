import { onMounted } from "vue";
import { storeToRefs } from "pinia";
import { useProjectsStore } from "../project.store";
import { useProjectRepository } from "./useProjectRepository";
import { useUserStore } from "@/feature/user";

// eslint-disable-next-line @typescript-eslint/explicit-module-boundary-types
export function useProjects() {
	const repository = useProjectRepository();
	const store = useProjectsStore();
	const userStore = useUserStore();

	onMounted(async () => {
		await refreshProjects();
	});

	async function refreshProjects(): Promise<void> {
		store.setProjects(await repository.getAllProjects());
		store.setAdminProjects(
			userStore.userDetails?.role === "ADMIN" ? await repository.getAdminProjects() : [],
		);
	}

	const { adminProjects, projects } = storeToRefs(store);

	return {
		projects,
		adminProjects,
		refreshProjects,
	};
}
