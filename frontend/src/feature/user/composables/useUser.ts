import { setTokens } from "@/api";
import { api, useUserStore } from "..";
import { storeToRefs } from "pinia";

// eslint-disable-next-line @typescript-eslint/explicit-module-boundary-types
export function useUser() {
	const userStore = useUserStore();

	async function login(username: string, password: string): Promise<void> {
		const tokens = await api.login(username, password);

		setTokens(tokens);

		const userDetails = await api.details();

		userStore.setUserDetails(userDetails);
	}

	const { userDetails } = storeToRefs(userStore);

	return { login, userDetails };
}
