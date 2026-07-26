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
	const hasLoadError = ref(false);

	async function loadUsers(): Promise<void> {
		isLoadingUsers.value = true;
		hasLoadError.value = false;

		try {
			store.setProjectUsers(await projectRepository.getProjectUsers(toValue(projectId)));
		} catch {
			hasLoadError.value = true;
		} finally {
			isLoadingUsers.value = false;
		}
	}

	async function addProjectUser(userId: number, role: ProjectRole): Promise<boolean> {
		hasLoadError.value = false;
		isAddingUser.value = true;

		try {
			await projectRepository.addProjectUser(toValue(projectId), userId, role);
			await loadUsers();
			return true;
		} catch {
			return false;
		} finally {
			isAddingUser.value = false;
		}
	}

	async function updateProjectUserRole(userId: number, role: ProjectRole): Promise<boolean> {
		hasLoadError.value = false;
		updatingUserRoleId.value = userId;

		try {
			await projectRepository.updateProjectUserRole(toValue(projectId), userId, role);
			await loadUsers();
			return true;
		} catch {
			return false;
		} finally {
			updatingUserRoleId.value = null;
		}
	}

	async function removeProjectUser(userId: number): Promise<boolean> {
		hasLoadError.value = false;
		removingUserId.value = userId;

		try {
			await projectRepository.removeProjectUser(toValue(projectId), userId);
			for (const ticket of store.tickets.filter((ticket) => ticket.assignedTo === userId)) {
				store.updateTicketAssignee(ticket.id, null);
			}
			await loadUsers();
			return true;
		} catch {
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
		hasLoadError,
		loadUsers,
		addProjectUser,
		updateProjectUserRole,
		removeProjectUser,
	};
}
