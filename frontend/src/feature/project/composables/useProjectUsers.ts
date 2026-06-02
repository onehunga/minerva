import { ref } from "vue";
import type { ProjectRole, ProjectUser } from "../project.model";
import { useProjectRepository } from "./useProjectRepository";

// eslint-disable-next-line @typescript-eslint/explicit-module-boundary-types
export function useProjectUsers(projectId: number) {
	const projectRepository = useProjectRepository();

	const users = ref<ProjectUser[]>([]);
	const isLoadingUsers = ref(false);
	const isAddingUser = ref(false);
	const errorMessage = ref("");
	const successMessage = ref("");

	async function loadUsers(): Promise<void> {
		isLoadingUsers.value = true;
		errorMessage.value = "";

		try {
			users.value = await projectRepository.getProjectUsers(projectId);
		} catch {
			errorMessage.value = "Projektbenutzer konnten nicht geladen werden.";
		} finally {
			isLoadingUsers.value = false;
		}
	}

	async function addProjectUser(userId: number, role: ProjectRole): Promise<boolean> {
		errorMessage.value = "";
		successMessage.value = "";
		isAddingUser.value = true;

		try {
			await projectRepository.addProjectUser(projectId, userId, role);
			await loadUsers();
			successMessage.value = "Benutzer wurde hinzugefügt.";
			return true;
		} catch {
			errorMessage.value = "Benutzer konnte nicht hinzugefügt werden.";
			return false;
		} finally {
			isAddingUser.value = false;
		}
	}

	return {
		users,
		isLoadingUsers,
		isAddingUser,
		errorMessage,
		successMessage,
		loadUsers,
		addProjectUser,
	};
}
