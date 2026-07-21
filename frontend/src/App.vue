<script setup lang="ts">
import { isAuthenticated } from "@/api";
import { useUser } from "@/feature/user";
import { useRouter } from "vue-router";

const router = useRouter();
const { logout } = useUser();

async function submitLogout(): Promise<void> {
	try {
		await logout();
	} catch {
		alert("Der Token konnte nicht widerrufen werden.");
	} finally {
		await router.push({ name: "login" });
	}
}
</script>

<template>
	<button v-if="isAuthenticated" type="button" @click="submitLogout">Abmelden</button>
	<RouterView />
</template>
