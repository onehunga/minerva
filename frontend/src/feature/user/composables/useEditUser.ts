import { computed, ref } from "vue";
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
	const errorMessage = ref("");
	const isUpdatingUser = ref(false);

	const trimmedUsername = computed((): string => username.value.trim());
	const hasUsernameChange = computed((): boolean => trimmedUsername.value !== user.username);
	const hasPasswordChange = computed((): boolean => password.value.length > 0);
	const hasRoleChange = computed((): boolean => role.value !== user.role);

	function validateForm(): boolean {
		if (!isUsernameValid(trimmedUsername.value)) {
			errorMessage.value =
				"Benutzername muss 3 bis 50 Zeichen lang sein und darf nur Buchstaben, Zahlen, '.', '_' oder '-' enthalten.";
			return false;
		}

		if (hasPasswordChange.value && password.value.length < MIN_PASSWORD_LENGTH) {
			errorMessage.value = "Passwort muss mindestens 8 Zeichen lang sein.";
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
		errorMessage.value = "";

		if (!validateForm()) {
			return false;
		}

		isUpdatingUser.value = true;

		try {
			if (hasRoleChange.value) {
				await userRepository.updateUserRole(user.id, role.value);
			}

			if (hasUsernameChange.value) {
				await userRepository.updateUsername(user.id, trimmedUsername.value);
			}

			if (hasPasswordChange.value) {
				await userRepository.updateUserPassword(user.id, password.value);
			}

			user.username = trimmedUsername.value;
			user.role = role.value;
			password.value = "";
			return true;
		} catch {
			errorMessage.value = `Benutzer "${user.username}" konnte nicht aktualisiert werden.`;
			return false;
		} finally {
			isUpdatingUser.value = false;
		}
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
