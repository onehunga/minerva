import { defineStore } from "pinia";
import { ref } from "vue";

import type { UserDetails } from "./user.model";

export const useUserStore = defineStore("user", function () {
	const userDetails = ref<UserDetails | null>(null);

	function setUserDetails(details: UserDetails | null): void {
		userDetails.value = details;
	}

	return { userDetails, setUserDetails };
});
