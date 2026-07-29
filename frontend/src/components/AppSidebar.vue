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
	SidebarMenuSub,
	SidebarMenuSubItem,
} from "@/components/ui/sidebar";
import { HugeiconsIcon } from "@hugeicons/vue";
import {
	Activity,
	BookOpen01Icon,
	Computer,
	Folder,
	Home,
	Logout,
	Moon,
	Paint,
	Plus,
	Sun,
	User,
	ChevronRight,
} from "@hugeicons/core-free-icons";
import { useProjects } from "@/feature/project";
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
import { Collapsible, CollapsibleContent, CollapsibleTrigger } from "@/components/ui/collapsible";

const { adminProjects, archivedProjects, projects } = useProjects();
const { logout, userDetails } = useUser();
const route = useRoute();
const router = useRouter();

const mode = useColorMode();

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
						<Collapsible
							v-if="archivedProjects.length > 0"
							as-child
							class="group/collapsible"
						>
							<SidebarMenuItem>
								<CollapsibleTrigger as-child>
									<SidebarMenuButton>
										<HugeiconsIcon
											:icon="ChevronRight"
											class="transition-transform duration-200 group-data-[state=open]/collapsible:rotate-90"
										/>
										<span>Archivierte Projekte</span>
									</SidebarMenuButton>
								</CollapsibleTrigger>
								<CollapsibleContent>
									<SidebarMenuSub>
										<SidebarMenuSubItem
											v-for="project in archivedProjects"
											:key="project.id"
										>
											<SidebarMenuButton
												:tooltip="project.name"
												@click="
													$router.push({
														name: 'project',
														params: { id: project.id },
													})
												"
											>
												<HugeiconsIcon :icon="Folder" />
												<span>{{ project.name }}</span>
											</SidebarMenuButton>
										</SidebarMenuSubItem>
									</SidebarMenuSub>
								</CollapsibleContent>
							</SidebarMenuItem>
						</Collapsible>
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
						<Collapsible as-child class="group/collapsible">
							<SidebarMenuItem v-if="adminProjects.length > 0">
								<CollapsibleTrigger as-child>
									<SidebarMenuButton>
										<HugeiconsIcon
											:icon="ChevronRight"
											class="transition-transform duration-200 group-data-[state=open]/collapsible:rotate-90"
										/>
										<span>Andere Projekte</span>
									</SidebarMenuButton>
								</CollapsibleTrigger>
								<CollapsibleContent>
									<SidebarMenuSub>
										<SidebarMenuSubItem
											v-for="project in adminProjects"
											:key="project.id"
										>
											<SidebarMenuButton
												:tooltip="project.name"
												@click="
													$router.push({
														name: 'project',
														params: { id: project.id },
													})
												"
											>
												<HugeiconsIcon :icon="Folder" />
												<span>{{ project.name }}</span>
											</SidebarMenuButton>
										</SidebarMenuSubItem>
									</SidebarMenuSub>
								</CollapsibleContent>
							</SidebarMenuItem>
						</Collapsible>
					</SidebarMenu>
				</SidebarGroupContent>
			</SidebarGroup>
		</SidebarContent>
		<SidebarFooter>
			<SidebarMenu>
				<SidebarMenuItem>
					<SidebarMenuButton as="a" href="/docs/" tooltip="Dokumentation">
						<HugeiconsIcon :icon="BookOpen01Icon" />
						<span>Dokumentation</span>
					</SidebarMenuButton>
				</SidebarMenuItem>
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
							<DropdownMenuItem @select="router.push({ name: 'profile' })">
								<HugeiconsIcon :icon="User" class="mr-2" />
								Profil
							</DropdownMenuItem>
							<DropdownMenuSeparator />
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
