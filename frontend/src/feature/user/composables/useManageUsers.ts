import { ref, type Ref } from "vue";
import type { UserRecord, UserRole } from "../user.model";
import { useUserRepository } from "./useUserRepository";

type ManageUsers = {
	users: Ref<UserRecord[]>;
	isLoadingUsers: Ref<boolean>;
	errorMessage: Ref<string>;
	deletingUserId: Ref<number | null>;
	updatingRoleUserId: Ref<number | null>;
	isCreatingUser: Ref<boolean>;
	successMessage: Ref<string>;
	loadUsers(): Promise<void>;
	createUser(username: string, password: string, role: UserRole): Promise<boolean>;
	updateUserRole(user: UserRecord, role: UserRole): Promise<boolean>;
	deleteUser(user: UserRecord): Promise<boolean>;
};

export function useManageUsers(): ManageUsers {
	const userRepository = useUserRepository();

	const users = ref<UserRecord[]>([]);
	const isLoadingUsers = ref(false);
	const errorMessage = ref("");
	const deletingUserId = ref<number | null>(null);
	const updatingRoleUserId = ref<number | null>(null);
	const isCreatingUser = ref(false);
	const successMessage = ref("");

	async function loadUsers(): Promise<void> {
		isLoadingUsers.value = true;
		errorMessage.value = "";

		try {
			const response = await userRepository.getAllUsers();
			users.value = response.users;
		} catch {
			errorMessage.value = "Benutzer konnten nicht geladen werden.";
		} finally {
			isLoadingUsers.value = false;
		}
	}

	async function createUser(
		username: string,
		password: string,
		role: UserRole,
	): Promise<boolean> {
		errorMessage.value = "";
		successMessage.value = "";
		isCreatingUser.value = true;

		try {
			await userRepository.createUser(username, password, role);
			successMessage.value = "Benutzer wurde erstellt.";
			return true;
		} catch {
			errorMessage.value = "Benutzer konnte nicht erstellt werden.";
			return false;
		} finally {
			isCreatingUser.value = false;
		}
	}

	async function updateUserRole(user: UserRecord, role: UserRole): Promise<boolean> {
		errorMessage.value = "";

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
			errorMessage.value = `Rolle von Benutzer "${user.username}" konnte nicht aktualisiert werden.`;
			return false;
		} finally {
			updatingRoleUserId.value = null;
		}
	}

	async function deleteUser(user: UserRecord): Promise<boolean> {
		errorMessage.value = "";
		deletingUserId.value = user.id;

		try {
			await userRepository.deleteUser(user.id);
			users.value = users.value.filter((currentUser) => currentUser.id !== user.id);
			return true;
		} catch {
			errorMessage.value = `Benutzer "${user.username}" konnte nicht gelöscht werden.`;
			return false;
		} finally {
			deletingUserId.value = null;
		}
	}

	return {
		users,
		isLoadingUsers,
		errorMessage,
		deletingUserId,
		updatingRoleUserId,
		isCreatingUser,
		successMessage,
		loadUsers,
		createUser,
		updateUserRole,
		deleteUser,
	};
}
