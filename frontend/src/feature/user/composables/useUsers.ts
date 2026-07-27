import { useUserRepository } from "./useUserRepository";
import { onMounted } from "vue";
import { ref } from "vue";
import type { UserRecord } from "../user.model";

// eslint-disable-next-line @typescript-eslint/explicit-module-boundary-types
export function useUsers() {
	const userRepository = useUserRepository();

	const users = ref<UserRecord[]>([]);
	const isLoadingUsers = ref(false);

	onMounted(async () => {
		await loadUsers();
	});

	async function loadUsers(): Promise<void> {
		isLoadingUsers.value = true;

		try {
			const response = await userRepository.getAllUsers();
			users.value = response.users;
		} catch {
			console.error("Benutzer konnten nicht geladen werden.");
		} finally {
			isLoadingUsers.value = false;
		}
	}

	return { users, isLoadingUsers, loadUsers };
}
