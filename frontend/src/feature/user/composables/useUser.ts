import { setTokens } from "@/api";
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

	const { userDetails } = storeToRefs(userStore);

	return { login, userDetails };
}
