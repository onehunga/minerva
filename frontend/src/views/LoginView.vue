<script setup lang="ts">
import type { AxiosResponse } from "axios";
import { ref, type Ref } from "vue";
import { useRouter, type Router } from "vue-router";

import { client, setTokens, type TokenPair } from "@/api";

const router: Router = useRouter();

const username: Ref<string> = ref("");
const password: Ref<string> = ref("");

async function login(): Promise<void> {
	try {
		const response: AxiosResponse<TokenPair> = await client.post<TokenPair>("/v1/auth/login", {
			username: username.value,
			password: password.value,
		});

		setTokens(response.data);
		await router.push("/");
	} catch {
		alert("Login failed");
	}
}
</script>

<template>
	<form @submit.prevent="login">
		<label for="username">Username</label>
		<input id="username" v-model="username" name="username" autocomplete="username" />

		<label for="password">Password</label>
		<input
			id="password"
			v-model="password"
			name="password"
			type="password"
			autocomplete="current-password"
		/>

		<button type="submit">Login</button>
	</form>
</template>
