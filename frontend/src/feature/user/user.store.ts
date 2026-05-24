import { defineStore } from "pinia";
import { ref } from "vue";

import type { model } from ".";

export const useUserStore = defineStore("user", function () {
	const userDetails = ref<model.UserDetails | null>(null);

	function setUserDetails(details: model.UserDetails | null): void {
		userDetails.value = details;
	}

	return { userDetails, setUserDetails };
});
