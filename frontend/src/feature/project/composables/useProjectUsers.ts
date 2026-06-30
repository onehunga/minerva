import { storeToRefs } from "pinia";
import { ref } from "vue";
import { useActiveProjectStore } from "../project.store";
import type { ProjectRole } from "../project.model";
import { useProjectRepository } from "./useProjectRepository";

// eslint-disable-next-line @typescript-eslint/explicit-module-boundary-types
export function useProjectUsers(projectId: number) {
	const projectRepository = useProjectRepository();
	const store = useActiveProjectStore();
	const { projectUsers: users } = storeToRefs(store);

	const isLoadingUsers = ref(false);
	const isAddingUser = ref(false);
	const updatingUserRoleId = ref<number | null>(null);
	const errorMessage = ref("");
	const successMessage = ref("");

	async function loadUsers(): Promise<void> {
		isLoadingUsers.value = true;
		errorMessage.value = "";

		try {
			store.setProjectUsers(await projectRepository.getProjectUsers(projectId));
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

	async function updateProjectUserRole(userId: number, role: ProjectRole): Promise<boolean> {
		errorMessage.value = "";
		successMessage.value = "";
		updatingUserRoleId.value = userId;

		try {
			await projectRepository.updateProjectUserRole(projectId, userId, role);
			await loadUsers();
			successMessage.value = "Projektrolle wurde aktualisiert.";
			return true;
		} catch {
			errorMessage.value = "Projektrolle konnte nicht aktualisiert werden.";
			return false;
		} finally {
			updatingUserRoleId.value = null;
		}
	}

	return {
		users,
		isLoadingUsers,
		isAddingUser,
		updatingUserRoleId,
		errorMessage,
		successMessage,
		loadUsers,
		addProjectUser,
		updateProjectUserRole,
	};
}
