<script setup lang="ts">
import { ref, type Ref } from "vue";
import { useRouter, type Router } from "vue-router";
import { useUser } from "@/feature/user/composables/useUser";

const router: Router = useRouter();

const { login } = useUser();

const username: Ref<string> = ref("");
const password: Ref<string> = ref("");

async function handleLogin(): Promise<void> {
	try {
		await login(username.value, password.value);

		await router.push("/");
	} catch {
		alert("Login failed");
	}
}
</script>

<template>
	<form @submit.prevent="handleLogin">
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
