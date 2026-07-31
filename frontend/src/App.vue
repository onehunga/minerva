<script setup lang="ts">
import { isAuthenticated } from "@/api";
import AppSidebar from "./components/AppSidebar.vue";
import { ScrollArea } from "./components/ui/scroll-area";
import { SidebarInset, SidebarProvider, SidebarTrigger } from "./components/ui/sidebar";
</script>

<template>
	<a
		href="#main-content"
		class="fixed top-2 left-2 z-50 -translate-y-16 rounded-md bg-background px-3 py-2 text-sm font-medium shadow focus:translate-y-0"
	>
		Zum Inhalt springen
	</a>
	<SidebarProvider>
		<AppSidebar v-if="isAuthenticated" />
		<SidebarInset class="h-svh overflow-hidden">
			<div v-if="isAuthenticated" class="p-2 pb-0 md:hidden">
				<SidebarTrigger />
			</div>
			<ScrollArea id="main-content" tabindex="-1" class="app-scroll-area min-h-0 flex-1">
				<div class="h-full p-4 md:p-6">
					<RouterView />
				</div>
			</ScrollArea>
		</SidebarInset>
	</SidebarProvider>
</template>

<style scoped>
.app-scroll-area :deep([data-slot="scroll-area-viewport"] > :first-child) {
	height: 100%;
}
</style>
