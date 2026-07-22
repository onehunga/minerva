<script setup lang="ts">
import { isAuthenticated } from "@/api";
import { useUser } from "@/feature/user";
import { useRouter } from "vue-router";
import AppSidebar from "./components/AppSidebar.vue";
import { SidebarInset, SidebarProvider } from "./components/ui/sidebar";
import { useColorMode } from "@vueuse/core";

const router = useRouter();
const { logout } = useUser();

const mode = useColorMode();
mode.value = "auto";

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
	<SidebarProvider>
		<AppSidebar v-if="isAuthenticated" />
		<SidebarInset>
			<button v-if="isAuthenticated" type="button" @click="submitLogout">Abmelden</button>
			<RouterView />
		</SidebarInset>
	</SidebarProvider>
</template>
