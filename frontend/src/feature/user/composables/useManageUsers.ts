import { ref, type Ref } from "vue";
import type { UserRecord, UserRole } from "../user.model";
import { useUserRepository } from "./useUserRepository";
import { useUsers } from "./useUsers";

type ManageUsers = {
	users: Ref<UserRecord[]>;
	isLoadingUsers: Ref<boolean>;
	errorMessage: Ref<string>;
	deletingUserId: Ref<number | null>;
	isCreatingUser: Ref<boolean>;
	updatingUserStateId: Ref<number | null>;
	successMessage: Ref<string>;
	loadUsers(): Promise<void>;
	createUser(username: string, password: string, role: UserRole): Promise<boolean>;
	deleteUser(user: UserRecord): Promise<boolean>;
	deactivateUser(user: UserRecord): Promise<boolean>;
	reactivateUser(user: UserRecord): Promise<boolean>;
};

export function useManageUsers(): ManageUsers {
	const userRepository = useUserRepository();

	const { users, isLoadingUsers, errorMessage, loadUsers } = useUsers();

	const deletingUserId = ref<number | null>(null);
	const isCreatingUser = ref(false);
	const successMessage = ref("");
	const updatingUserStateId = ref<number | null>(null);

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

	async function updateUserState(user: UserRecord, deactivated: boolean): Promise<boolean> {
		errorMessage.value = "";
		updatingUserStateId.value = user.id;

		try {
			await (deactivated
				? userRepository.deactivateUser(user.id)
				: userRepository.reactivateUser(user.id));
			user.deactivated = deactivated;
			return true;
		} catch {
			errorMessage.value = `Benutzer "${user.username}" konnte nicht ${deactivated ? "deaktiviert" : "reaktiviert"} werden.`;
			return false;
		} finally {
			updatingUserStateId.value = null;
		}
	}

	return {
		users,
		isLoadingUsers,
		errorMessage,
		deletingUserId,
		isCreatingUser,
		updatingUserStateId,
		successMessage,
		loadUsers,
		createUser,
		deleteUser,
		deactivateUser: (user: UserRecord) => updateUserState(user, true),
		reactivateUser: (user: UserRecord) => updateUserState(user, false),
	};
}
