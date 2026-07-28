import { computed, ref } from "vue";
import { storeToRefs } from "pinia";
import { useUserStore } from "../user.store";
import { useUserRepository } from "./useUserRepository";

// eslint-disable-next-line @typescript-eslint/explicit-module-boundary-types
export function useProfile() {
	const userRepository = useUserRepository();
	const userStore = useUserStore();
	const { userDetails } = storeToRefs(userStore);

	const username = ref(userDetails.value?.username ?? "");
	const password = ref("");
	const passwordConfirmation = ref("");
	const usernameError = ref("");
	const usernameSuccess = ref("");
	const passwordError = ref("");
	const passwordSuccess = ref("");
	const isUpdatingUsername = ref(false);
	const isUpdatingPassword = ref(false);
	const isUsernameChanged = computed(() => username.value.trim() !== userDetails.value?.username);

	async function updateUsername(): Promise<void> {
		usernameError.value = "";
		usernameSuccess.value = "";
		const value = username.value.trim();

		const currentUser = userDetails.value;
		if (currentUser === null || !isUsernameChanged.value) {
			return;
		}

		isUpdatingUsername.value = true;

		try {
			await userRepository.updateUsername(currentUser.id, value);
			if (userDetails.value?.id === currentUser.id) {
				userStore.setUserDetails({ ...currentUser, username: value });
			}
			username.value = value;
			usernameSuccess.value = "Benutzername wurde aktualisiert.";
		} catch {
			usernameError.value = "Benutzername konnte nicht aktualisiert werden.";
		} finally {
			isUpdatingUsername.value = false;
		}
	}

	async function updatePassword(): Promise<void> {
		passwordError.value = "";
		passwordSuccess.value = "";

		if (password.value.length < 8) {
			passwordError.value = "Passwort muss mindestens 8 Zeichen lang sein.";
			return;
		}

		if (password.value !== passwordConfirmation.value) {
			passwordError.value = "Die Passwörter stimmen nicht überein.";
			return;
		}

		const currentUser = userDetails.value;
		if (currentUser === null) {
			return;
		}

		isUpdatingPassword.value = true;

		try {
			await userRepository.updateUserPassword(currentUser.id, password.value);
			password.value = "";
			passwordConfirmation.value = "";
			passwordSuccess.value = "Passwort wurde aktualisiert.";
		} catch {
			passwordError.value = "Passwort konnte nicht aktualisiert werden.";
		} finally {
			isUpdatingPassword.value = false;
		}
	}

	return {
		username,
		password,
		passwordConfirmation,
		usernameError,
		usernameSuccess,
		passwordError,
		passwordSuccess,
		isUpdatingUsername,
		isUpdatingPassword,
		isUsernameChanged,
		updateUsername,
		updatePassword,
	};
}
