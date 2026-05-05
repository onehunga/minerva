<script setup lang="ts">
import type { AxiosResponse } from "axios";
import { onMounted, ref, type Ref } from "vue";

import { client } from "@/api";

type UserDetailsResponse = {
	username: string;
};

const username: Ref<string | null> = ref(null);
const isLoading: Ref<boolean> = ref(true);

onMounted(async (): Promise<void> => {
	try {
		const response: AxiosResponse<UserDetailsResponse> =
			await client.get<UserDetailsResponse>("/v1/users/me");

		username.value = response.data.username;
	} catch {
		alert("Could not load user");
	} finally {
		isLoading.value = false;
	}
});
</script>

<template>
	<p v-if="isLoading">Loading...</p>
	<p v-else>{{ username }}</p>
</template>
