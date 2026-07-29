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
		const isAdmin = userStore.userDetails?.role === "ADMIN";
		const [projects, archivedProjects, adminProjects, archivedAdminProjects] =
			await Promise.all([
				repository.getAllProjects(),
				repository.getAllProjects(true),
				isAdmin ? repository.getAdminProjects() : [],
				isAdmin ? repository.getAdminProjects(true) : [],
			]);
		store.setProjects(projects);
		store.setAdminProjects(adminProjects);
		store.setArchivedProjects([
			...archivedProjects,
			...archivedAdminProjects.filter(
				(project) =>
					!archivedProjects.some((memberProject) => memberProject.id === project.id),
			),
		]);
	}

	const { adminProjects, archivedProjects, projects } = storeToRefs(store);

	return {
		projects,
		adminProjects,
		archivedProjects,
		refreshProjects,
	};
}
