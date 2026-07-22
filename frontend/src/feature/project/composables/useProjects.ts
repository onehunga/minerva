import { onMounted } from "vue";
import { useProjectRepository, useProjectsStore } from "..";
import { storeToRefs } from "pinia";

// eslint-disable-next-line @typescript-eslint/explicit-module-boundary-types
export function useProjects() {
	const repository = useProjectRepository();
	const store = useProjectsStore();

	onMounted(async () => {
		await refreshProjects();
	});

	async function refreshProjects(): Promise<void> {
		store.setProjects(await repository.getAllProjects());
	}

	const { projects } = storeToRefs(store);

	return {
		projects,
		refreshProjects,
	};
}
