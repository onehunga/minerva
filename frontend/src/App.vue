<script setup lang="ts">
import { isAuthenticated } from "@/api";
import AppSidebar from "./components/AppSidebar.vue";
import { SidebarInset, SidebarProvider, SidebarTrigger } from "./components/ui/sidebar";
import { useColorMode } from "@vueuse/core";

const mode = useColorMode();
mode.value = "auto";
</script>

<template>
	<SidebarProvider>
		<AppSidebar v-if="isAuthenticated" />
		<SidebarInset>
			<div v-if="isAuthenticated" class="p-2 pb-0 md:hidden">
				<SidebarTrigger />
			</div>
			<div class="flex-1 p-4 md:p-6">
				<RouterView />
			</div>
		</SidebarInset>
	</SidebarProvider>
</template>
