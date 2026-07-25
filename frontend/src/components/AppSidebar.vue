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
	SidebarRail,
} from "@/components/ui/sidebar";
import { HugeiconsIcon } from "@hugeicons/vue";
import {
	Activity,
	Computer,
	Folder,
	Home,
	Logout,
	Moon,
	Paint,
	Plus,
	Sun,
	User,
} from "@hugeicons/core-free-icons";
import { useProjects } from "@/feature/project/composables/useProjects";
import { useUser } from "@/feature/user";
import { useColorMode } from "@vueuse/core";
import { useRoute, useRouter } from "vue-router";
import {
	DropdownMenu,
	DropdownMenuContent,
	DropdownMenuLabel,
	DropdownMenuSeparator,
	DropdownMenuTrigger,
	DropdownMenuItem,
} from "@/components/ui/dropdown-menu";

const { projects } = useProjects();
const { logout, userDetails } = useUser();
const route = useRoute();
const router = useRouter();

const mode = useColorMode();
mode.value = "dark";

async function submitLogout(): Promise<void> {
	await logout();
	await router.push({ name: "login" });
}
</script>

<template>
	<Sidebar variant="floating" collapsible="icon">
		<SidebarHeader>
			<SidebarMenu class="flex-row items-center">
				<SidebarMenuItem class="min-w-0 flex-1 group-data-[collapsible=icon]:hidden">
					<SidebarMenuButton tooltip="Minerva" @click="$router.push('/')">
						<HugeiconsIcon :icon="Home" />
						<span>Minerva</span>
					</SidebarMenuButton>
				</SidebarMenuItem>
				<SidebarMenuItem class="shrink-0 group-data-[collapsible=icon]:mx-auto">
					<SidebarTrigger class="size-8" />
				</SidebarMenuItem>
			</SidebarMenu>
		</SidebarHeader>
		<SidebarContent>
			<SidebarGroup>
				<SidebarGroupLabel>Navigation</SidebarGroupLabel>
				<SidebarGroupContent>
					<SidebarMenu>
						<SidebarMenuItem>
							<SidebarMenuButton
								tooltip="Übersicht"
								:is-active="route.name === 'landing'"
								@click="$router.push('/')"
							>
								<HugeiconsIcon :icon="Home" />
								<span>Übersicht</span>
							</SidebarMenuButton>
						</SidebarMenuItem>
						<SidebarMenuItem>
							<SidebarMenuButton
								tooltip="Projekte"
								:is-active="route.name === 'projects'"
								@click="$router.push({ name: 'projects' })"
							>
								<HugeiconsIcon :icon="Folder" />
								<span>Projekte</span>
							</SidebarMenuButton>
						</SidebarMenuItem>
						<SidebarMenuItem>
							<SidebarMenuButton
								tooltip="Aktivitäten"
								:is-active="route.name === 'activities'"
								@click="$router.push({ name: 'activities' })"
							>
								<HugeiconsIcon :icon="Activity" />
								<span>Aktivitäten</span>
							</SidebarMenuButton>
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
								:tooltip="project.name"
								@click="
									$router.push({ name: 'project', params: { id: project.id } })
								"
							>
								<HugeiconsIcon :icon="Folder" />
								<span>{{ project.name }}</span>
							</SidebarMenuButton>
						</SidebarMenuItem>
						<SidebarMenuItem>
							<SidebarMenuButton
								tooltip="Projekt Erstellen"
								@click="$router.push('/create-project')"
							>
								<div class="flex flex-row items-center gap-2">
									<HugeiconsIcon :icon="Plus" class="" />
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
							<SidebarMenuButton
								tooltip="Benutzer"
								:is-active="route.name === 'admin-users'"
								@click="$router.push({ name: 'admin-users' })"
							>
								<HugeiconsIcon :icon="User" />
								<span>Benutzer</span>
							</SidebarMenuButton>
						</SidebarMenuItem>
					</SidebarMenu>
				</SidebarGroupContent>
			</SidebarGroup>
		</SidebarContent>
		<SidebarFooter>
			<SidebarMenu>
				<SidebarMenuItem>
					<DropdownMenu>
						<DropdownMenuTrigger as-child>
							<SidebarMenuButton tooltip="Farbmodus">
								<HugeiconsIcon
									:icon="mode === 'dark' ? Moon : mode === 'light' ? Sun : Paint"
								/>
								<span>{{
									mode === "dark"
										? "Dunkler Modus"
										: mode == "light"
											? "Heller Modus"
											: "System"
								}}</span>
							</SidebarMenuButton>
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
				<SidebarMenuItem>
					<DropdownMenu>
						<DropdownMenuTrigger as-child>
							<SidebarMenuButton :tooltip="userDetails?.username">
								<HugeiconsIcon :icon="User" />
								<span>{{ userDetails?.username }}</span>
							</SidebarMenuButton>
						</DropdownMenuTrigger>
						<DropdownMenuContent side="top" align="end">
							<DropdownMenuItem @select="submitLogout">
								<HugeiconsIcon :icon="Logout" class="mr-2" />
								Abmelden
							</DropdownMenuItem>
						</DropdownMenuContent>
					</DropdownMenu>
				</SidebarMenuItem>
			</SidebarMenu>
		</SidebarFooter>
		<SidebarRail />
	</Sidebar>
</template>
