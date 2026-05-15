import { clearTokens, initializeAuth, isAuthenticated } from "@/api";

import { details } from "./user.api";
import { useUserStore } from "./user.store";

export async function initializeUserSession(): Promise<void> {
	const userStore = useUserStore();

	await initializeAuth();

	if (!isAuthenticated.value) {
		userStore.setUserDetails(null);
		return;
	}

	try {
		userStore.setUserDetails(await details());
	} catch {
		clearTokens();
		userStore.setUserDetails(null);
	}
}
