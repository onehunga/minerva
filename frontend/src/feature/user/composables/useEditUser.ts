import { ref } from "vue";
import type { UserRecord, UserRole } from "../user.model";
import { useUserRepository } from "./useUserRepository";

const USERNAME_PATTERN = /^[A-Za-z0-9._-]+$/;
const MIN_USERNAME_LENGTH = 3;
const MAX_USERNAME_LENGTH = 50;
const MIN_PASSWORD_LENGTH = 8;

// eslint-disable-next-line @typescript-eslint/explicit-module-boundary-types
export function useEditUser(user: UserRecord) {
	const userRepository = useUserRepository();

	const username = ref(user.username);
	const password = ref("");
	const role = ref<UserRole>(user.role);
	const errorMessage = ref<string[]>([]);
	const isUpdatingUser = ref(false);

	function validateForm(): boolean {
		if (!isUsernameValid(username.value.trim())) {
			errorMessage.value = [
				"Benutzername muss 3 bis 50 Zeichen lang sein und darf nur Buchstaben, Zahlen, '.', '_' oder '-' enthalten.",
			];
			return false;
		}

		if (password.value.length > 0 && password.value.length < MIN_PASSWORD_LENGTH) {
			errorMessage.value = ["Passwort muss mindestens 8 Zeichen lang sein."];
			return false;
		}

		return true;
	}

	function isUsernameValid(value: string): boolean {
		return (
			value.length >= MIN_USERNAME_LENGTH &&
			value.length <= MAX_USERNAME_LENGTH &&
			USERNAME_PATTERN.test(value)
		);
	}

	async function updateUser(): Promise<boolean> {
		errorMessage.value = [];

		if (!validateForm()) {
			return false;
		}

		isUpdatingUser.value = true;

		try {
			if (role.value !== user.role) {
				await userRepository.updateUserRole(user.id, role.value);
			}

			user.role = role.value;
		} catch {
			errorMessage.value.push(
				`Rolle des Benutzers "${user.username}" konnte nicht aktualisiert werden.`,
			);
		}

		try {
			if (username.value.trim() !== user.username) {
				await userRepository.updateUsername(user.id, username.value.trim());
			}

			user.username = username.value.trim();
		} catch {
			errorMessage.value.push(
				`Benutzername "${user.username}" konnte nicht aktualisiert werden.`,
			);
		}

		try {
			if (password.value.trim().length > 0) {
				await userRepository.updateUserPassword(user.id, password.value);
			}

			password.value = "";
		} catch {
			errorMessage.value.push(
				`Passwort des Benutzers "${user.username}" konnte nicht aktualisiert werden.`,
			);
		}

		isUpdatingUser.value = false;

		return true;
	}

	return {
		username,
		password,
		role,
		errorMessage,
		isUpdatingUser,
		updateUser,
	};
}
