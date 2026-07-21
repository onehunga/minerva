import { clearTokens, getRefreshToken, setTokens } from "@/api";
import { useUserStore, useUserRepository } from "..";
import { storeToRefs } from "pinia";

// eslint-disable-next-line @typescript-eslint/explicit-module-boundary-types
export function useUser() {
	const userStore = useUserStore();

	const userRepository = useUserRepository();

	async function login(username: string, password: string): Promise<void> {
		const tokens = await userRepository.login(username, password);

		setTokens(tokens);

		const userDetails = await userRepository.details();

		userStore.setUserDetails(userDetails);
	}

	async function logout(): Promise<void> {
		const refreshToken = getRefreshToken();

		try {
			if (refreshToken !== null) {
				await userRepository.logout(refreshToken);
			}
		} finally {
			clearTokens();
			userStore.setUserDetails(null);
		}
	}

	const { userDetails } = storeToRefs(userStore);

	return { login, logout, userDetails };
}
