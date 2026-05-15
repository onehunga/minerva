import { defineStore } from "pinia";
import { ref } from "vue";

import type { api } from ".";

export const useUserStore = defineStore("user", function () {
	const userDetails = ref<api.UserDetails | null>(null);

	function setUserDetails(details: api.UserDetails | null): void {
		userDetails.value = details;
	}

	return { userDetails, setUserDetails };
});
