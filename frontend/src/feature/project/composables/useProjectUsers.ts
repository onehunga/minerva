import { storeToRefs } from "pinia";
import { ref, toValue, type MaybeRefOrGetter } from "vue";
import { useActiveProjectStore } from "../project.store";
import type { ProjectRole } from "../project.model";
import { useProjectRepository } from "./useProjectRepository";

// eslint-disable-next-line @typescript-eslint/explicit-module-boundary-types
export function useProjectUsers(projectId: MaybeRefOrGetter<number>) {
	const projectRepository = useProjectRepository();
	const store = useActiveProjectStore();
	const { projectUsers: users } = storeToRefs(store);

	const isLoadingUsers = ref(false);
	const isAddingUser = ref(false);
	const updatingUserRoleId = ref<number | null>(null);
	const removingUserId = ref<number | null>(null);
	const errorMessage = ref("");
	const successMessage = ref("");

	async function loadUsers(): Promise<void> {
		isLoadingUsers.value = true;
		errorMessage.value = "";

		try {
			store.setProjectUsers(await projectRepository.getProjectUsers(toValue(projectId)));
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
			await projectRepository.addProjectUser(toValue(projectId), userId, role);
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
			await projectRepository.updateProjectUserRole(toValue(projectId), userId, role);
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

	async function removeProjectUser(userId: number): Promise<boolean> {
		errorMessage.value = "";
		successMessage.value = "";
		removingUserId.value = userId;

		try {
			await projectRepository.removeProjectUser(toValue(projectId), userId);
			for (const ticket of store.tickets.filter((ticket) => ticket.assignedTo === userId)) {
				store.updateTicketAssignee(ticket.id, null);
			}
			await loadUsers();
			successMessage.value = "Projektmitglied wurde entfernt.";
			return true;
		} catch {
			errorMessage.value = "Projektmitglied konnte nicht entfernt werden.";
			return false;
		} finally {
			removingUserId.value = null;
		}
	}

	return {
		users,
		isLoadingUsers,
		isAddingUser,
		updatingUserRoleId,
		removingUserId,
		errorMessage,
		successMessage,
		loadUsers,
		addProjectUser,
		updateProjectUserRole,
		removeProjectUser,
	};
}
