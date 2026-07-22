<script setup lang="ts">
import {
	Sidebar,
	SidebarHeader,
	SidebarContent,
	SidebarFooter,
	SidebarMenu,
	SidebarMenuItem,
	SidebarMenuButton,
	SidebarTrigger,
	SidebarGroup,
	SidebarGroupContent,
	SidebarGroupLabel,
} from "@/components/ui/sidebar";
import { HugeiconsIcon } from "@hugeicons/vue";
import { Plus, Sun, Moon, Computer } from "@hugeicons/core-free-icons";
import { useProjects } from "@/feature/project/composables/useProjects";
import { useUser } from "@/feature/user";
import { useColorMode } from "@vueuse/core";
import {
	DropdownMenu,
	DropdownMenuContent,
	DropdownMenuLabel,
	DropdownMenuSeparator,
	DropdownMenuTrigger,
	DropdownMenuItem,
} from "@/components/ui/dropdown-menu";

const { projects } = useProjects();
const { userDetails } = useUser();

const mode = useColorMode();
mode.value = "dark";
</script>

<template>
	<Sidebar class="bg-sidebar">
		<SidebarHeader>
			<SidebarMenu>
				<SidebarMenuItem>
					<SidebarMenuButton @click="$router.push('/')" size="lg">
						Minerva
					</SidebarMenuButton>
				</SidebarMenuItem>
			</SidebarMenu>
		</SidebarHeader>
		<SidebarContent>
			<SidebarGroup>
				<SidebarGroupLabel>Navigation</SidebarGroupLabel>
				<SidebarGroupContent>
					<SidebarMenu>
						<SidebarMenuItem>
							<SidebarMenuButton @click="$router.push('/')"
								>Übersicht</SidebarMenuButton
							>
						</SidebarMenuItem>
						<SidebarMenuItem>
							<SidebarMenuButton>Projekte</SidebarMenuButton>
						</SidebarMenuItem>
					</SidebarMenu>
				</SidebarGroupContent>
			</SidebarGroup>

			<SidebarGroup>
				<SidebarGroupLabel>Projekte</SidebarGroupLabel>
				<SidebarGroupContent>
					<SidebarMenu>
						<SidebarMenuItem class="m-1" v-for="project in projects" :key="project.id">
							<SidebarMenuButton
								@click="
									$router.push({ name: 'project', params: { id: project.id } })
								"
							>
								{{ project.name }}
							</SidebarMenuButton>
						</SidebarMenuItem>
						<SidebarMenuItem>
							<SidebarMenuButton @click="$router.push('/create-project')">
								<div
									class="flex items-center justify-center w-6 h-6 rounded-full bg-sidebar-primary text-sidebar-primary-foreground"
								>
									<HugeiconsIcon :icon="Plus" class="" />
								</div>
								<div>
									<p>Projekt erstellen</p>
								</div>
							</SidebarMenuButton>
						</SidebarMenuItem>
					</SidebarMenu>
				</SidebarGroupContent>
			</SidebarGroup>

			<SidebarGroup v-if="userDetails?.role === 'ADMIN'">
				<SidebarGroupLabel>Administration</SidebarGroupLabel>
				<SidebarGroupContent>
					<SidebarMenu>
						<SidebarMenuItem>
							<SidebarMenuButton @click="$router.push('/admin')"
								>Benutzer</SidebarMenuButton
							>
						</SidebarMenuItem>
					</SidebarMenu>
				</SidebarGroupContent>
			</SidebarGroup>
		</SidebarContent>
		<SidebarFooter>
			<SidebarMenu>
				<SidebarMenuItem>
					<DropdownMenu>
						<DropdownMenuTrigger>
							{{
								mode === "dark"
									? "Dark Mode"
									: mode == "light"
										? "Light Mode"
										: "System"
							}}
						</DropdownMenuTrigger>
						<DropdownMenuContent>
							<DropdownMenuLabel>Farbmodus</DropdownMenuLabel>
							<DropdownMenuSeparator></DropdownMenuSeparator>
							<DropdownMenuItem @click="mode = 'auto'">
								<HugeiconsIcon :icon="Computer" class="mr-2" />
								System
							</DropdownMenuItem>
							<DropdownMenuItem @click="mode = 'light'">
								<HugeiconsIcon :icon="Sun" class="mr-2" />
								Hell
							</DropdownMenuItem>
							<DropdownMenuItem @click="mode = 'dark'">
								<HugeiconsIcon :icon="Moon" class="mr-2" />
								Dunkel
							</DropdownMenuItem>
						</DropdownMenuContent>
					</DropdownMenu>
				</SidebarMenuItem>
			</SidebarMenu>
		</SidebarFooter>
	</Sidebar>
	<SidebarTrigger></SidebarTrigger>
</template>
