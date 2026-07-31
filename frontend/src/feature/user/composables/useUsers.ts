import { useUserRepository } from "./useUserRepository";
import { onMounted } from "vue";
import { ref } from "vue";
import type { UserRecord } from "../user.model";

// eslint-disable-next-line @typescript-eslint/explicit-module-boundary-types
export function useUsers() {
	const userRepository = useUserRepository();

	const users = ref<UserRecord[]>([]);
	const isLoadingUsers = ref(false);
	const errorMessage = ref("");

	onMounted(async () => {
		await loadUsers();
	});

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

	return { users, isLoadingUsers, errorMessage, loadUsers };
}
