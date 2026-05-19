import { ref, type Ref } from "vue";
import type { UserRecord, UserRole } from "../user.model";
import { useUserRepository } from "./useUserRepository";

type ManageUsers = {
	users: Ref<UserRecord[]>;
	isLoadingUsers: Ref<boolean>;
	loadErrorMessage: Ref<string>;
	deleteErrorMessage: Ref<string>;
	roleErrorMessage: Ref<string>;
	deletingUserId: Ref<number | null>;
	updatingRoleUserId: Ref<number | null>;
	isCreatingUser: Ref<boolean>;
	createErrorMessage: Ref<string>;
	createSuccessMessage: Ref<string>;
	loadUsers(): Promise<void>;
	createUser(username: string, password: string, role: UserRole): Promise<boolean>;
	updateUserRole(user: UserRecord, role: UserRole): Promise<boolean>;
	deleteUser(user: UserRecord): Promise<boolean>;
};

export function useManageUsers(): ManageUsers {
	const userRepository = useUserRepository();

	const users = ref<UserRecord[]>([]);
	const isLoadingUsers = ref(false);
	const loadErrorMessage = ref("");
	const deleteErrorMessage = ref("");
	const roleErrorMessage = ref("");
	const deletingUserId = ref<number | null>(null);
	const updatingRoleUserId = ref<number | null>(null);
	const isCreatingUser = ref(false);
	const createErrorMessage = ref("");
	const createSuccessMessage = ref("");

	async function loadUsers(): Promise<void> {
		isLoadingUsers.value = true;
		loadErrorMessage.value = "";
		deleteErrorMessage.value = "";
		roleErrorMessage.value = "";

		try {
			const response = await userRepository.getAllUsers();
			users.value = response.users;
		} catch {
			loadErrorMessage.value = "Benutzer konnten nicht geladen werden.";
		} finally {
			isLoadingUsers.value = false;
		}
	}

	async function createUser(
		username: string,
		password: string,
		role: UserRole,
	): Promise<boolean> {
		createErrorMessage.value = "";
		createSuccessMessage.value = "";
		isCreatingUser.value = true;

		try {
			await userRepository.createUser(username, password, role);
			createSuccessMessage.value = "Benutzer wurde erstellt.";
			return true;
		} catch {
			createErrorMessage.value = "Benutzer konnte nicht erstellt werden.";
			return false;
		} finally {
			isCreatingUser.value = false;
		}
	}

	async function updateUserRole(user: UserRecord, role: UserRole): Promise<boolean> {
		roleErrorMessage.value = "";

		if (user.role === role) {
			return true;
		}

		const previousRole = user.role;
		updatingRoleUserId.value = user.id;

		try {
			await userRepository.updateUserRole(user.id, role);
			user.role = role;
			return true;
		} catch {
			user.role = previousRole;
			roleErrorMessage.value = `Rolle von Benutzer "${user.username}" konnte nicht aktualisiert werden.`;
			return false;
		} finally {
			updatingRoleUserId.value = null;
		}
	}

	async function deleteUser(user: UserRecord): Promise<boolean> {
		deleteErrorMessage.value = "";
		deletingUserId.value = user.id;

		try {
			await userRepository.deleteUser(user.id);
			users.value = users.value.filter((currentUser) => currentUser.id !== user.id);
			return true;
		} catch {
			deleteErrorMessage.value = `Benutzer "${user.username}" konnte nicht gelöscht werden.`;
			return false;
		} finally {
			deletingUserId.value = null;
		}
	}

	return {
		users,
		isLoadingUsers,
		loadErrorMessage,
		deleteErrorMessage,
		roleErrorMessage,
		deletingUserId,
		updatingRoleUserId,
		isCreatingUser,
		createErrorMessage,
		createSuccessMessage,
		loadUsers,
		createUser,
		updateUserRole,
		deleteUser,
	};
}
