<script setup lang="ts">
import { isAuthenticated } from "@/api";
import AppSidebar from "./components/AppSidebar.vue";
import { SidebarInset, SidebarProvider } from "./components/ui/sidebar";
import { useColorMode } from "@vueuse/core";

const mode = useColorMode();
mode.value = "auto";
</script>

<template>
	<SidebarProvider>
		<AppSidebar v-if="isAuthenticated" />
		<SidebarInset>
			<RouterView />
		</SidebarInset>
	</SidebarProvider>
</template>
