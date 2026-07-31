<script setup lang="ts">
import { computed, ref, watch } from "vue";
import { useRouter } from "vue-router";
import { ActivityTimeline, useProjectActivities } from "@/feature/activity";
import { DashboardOverview, useProjectDashboard } from "@/feature/dashboard";
import { CreateTicketForm, TicketDetail, TicketList } from "@/feature/ticket";
import { useUserStore } from "@/feature/user";
import { Alert, AlertAction, AlertDescription, AlertTitle } from "@/components/ui/alert";
import {
	AlertDialog,
	AlertDialogCancel,
	AlertDialogContent,
	AlertDialogDescription,
	AlertDialogFooter,
	AlertDialogHeader,
	AlertDialogTitle,
} from "@/components/ui/alert-dialog";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Separator } from "@/components/ui/separator";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { HugeiconsIcon } from "@hugeicons/vue";
import { FolderArchiveIcon } from "@hugeicons/core-free-icons";
import { useActiveProjectStore, useProjectsStore } from "../project.store";
import { useProject } from "../composables/useProject";
import { useProjectRepository } from "../composables/useProjectRepository";
import ProjectUserManagement from "./ProjectUserManagement.vue";
import { Textarea } from "@/components/ui/textarea";

const props = defineProps<{
	id: number;
}>();

const { details, tickets, ticketTypes, fetchTickets, updateProjectDetails } = useProject();
const projectRepository = useProjectRepository();
const router = useRouter();
const userStore = useUserStore();
const selectedTicketId = ref<number | null>(null);
const showArchived = ref(false);
const isLoadingTickets = ref(false);
const isRestoringTicket = ref(false);
const isArchivingProject = ref(false);
const isRestoringProject = ref(false);
const isArchiveDialogOpen = ref(false);
const isDeletingProject = ref(false);
const isDeleteDialogOpen = ref(false);
const isUpdatingDetails = ref(false);
const errorMessage = ref("");
const draftName = ref("");
const draftDescription = ref("");
const showMobileTicketDetail = ref(false);

const isAdmin = computed(() => userStore.userDetails?.role === "ADMIN");
const canUpdateDetails = computed(() => details.value?.projectRole === "OWNER" || isAdmin.value);
const canModifyProject = computed(
	() => canUpdateDetails.value && details.value?.archived === false,
);
const canModifyTickets = computed(() => isAdmin.value || details.value?.projectRole !== null);
const canOpenSettings = canUpdateDetails;
const projectRoleLabel = computed(() => {
	switch (details.value?.projectRole) {
		case "OWNER":
			return "Owner";
		case "CONTRIBUTOR":
			return "Mitwirkend";
		case "VIEWER":
			return "Lesend";
		default:
			return "Keine Mitgliedschaft";
	}
});
const openTicketCount = computed(
	() =>
		(projectDashboard.value?.ticketsByCategory.open ?? 0) +
		(projectDashboard.value?.ticketsByCategory.inProgress ?? 0),
);
const selectedTicket = computed(() =>
	tickets.value.find((ticket) => ticket.id === selectedTicketId.value),
);
const selectedTicketType = computed(() =>
	ticketTypes.value.find((ticketType) => ticketType.id === selectedTicket.value?.ticketTypeId),
);
const selectedTicketParentName = computed(
	() => tickets.value.find((ticket) => ticket.id === selectedTicket.value?.parentTicketId)?.name,
);
const selectedTicketChildren = computed(() =>
	tickets.value.filter((ticket) => ticket.parentTicketId === selectedTicket.value?.id),
);

const {
	events: activityEvents,
	isLoading: isActivityLoading,
	errorMessage: activityError,
} = useProjectActivities(() => props.id);
const {
	data: projectDashboard,
	isLoading: isProjectDashboardLoading,
	errorMessage: projectDashboardError,
} = useProjectDashboard(() => props.id);

watch(
	() => props.id,
	() => {
		errorMessage.value = "";
		isArchiveDialogOpen.value = false;
		isRestoringTicket.value = false;
		showArchived.value = false;
		selectedTicketId.value = null;
		showMobileTicketDetail.value = false;
	},
);

watch(
	() => [details.value?.name, details.value?.description],
	() => {
		draftName.value = details.value?.name ?? "";
		draftDescription.value = details.value?.description ?? "";
	},
	{ immediate: true },
);

watch(
	tickets,
	() => {
		if (!tickets.value.some((ticket) => ticket.id === selectedTicketId.value)) {
			isRestoringTicket.value = false;
			selectedTicketId.value = tickets.value[0]?.id ?? null;
		}
	},
	{ immediate: true },
);

watch(showArchived, async (archived) => {
	isLoadingTickets.value = true;
	errorMessage.value = "";

	try {
		await fetchTickets(archived);
	} catch {
		errorMessage.value = "Die Tickets konnten nicht geladen werden.";
	} finally {
		isLoadingTickets.value = false;
	}
});

function selectTicket(ticketId: number): void {
	selectedTicketId.value = ticketId;
	showMobileTicketDetail.value = true;
}

async function submitDetailsUpdate(): Promise<void> {
	if (
		isUpdatingDetails.value ||
		!canModifyProject.value ||
		details.value == null ||
		!draftName.value.trim()
	) {
		return;
	}

	const name = draftName.value.trim();
	const description = draftDescription.value.trim();

	if (name === details.value.name && description === details.value.description) {
		return;
	}

	isUpdatingDetails.value = true;
	errorMessage.value = "";

	try {
		await updateProjectDetails(name, description);
	} catch {
		errorMessage.value = "Die Projektdetails konnten nicht aktualisiert werden.";
	} finally {
		isUpdatingDetails.value = false;
	}
}

function updateArchiveDialogOpen(open: boolean): void {
	if (!open && !isArchivingProject.value) {
		isArchiveDialogOpen.value = false;
	}
}

function openArchiveDialog(): void {
	if (details.value?.archived !== false) {
		return;
	}

	errorMessage.value = "";
	isArchiveDialogOpen.value = true;
}

async function submitArchiveProject(): Promise<void> {
	if (isArchivingProject.value || details.value?.archived !== false) {
		return;
	}

	isArchivingProject.value = true;
	errorMessage.value = "";

	try {
		await projectRepository.archiveProject(props.id);
		const projectsStore = useProjectsStore();
		const project = {
			id: props.id,
			name: details.value.name,
			description: details.value.description,
		};
		projectsStore.setProjects(projectsStore.projects.filter((item) => item.id !== props.id));
		projectsStore.setAdminProjects(
			projectsStore.adminProjects.filter((item) => item.id !== props.id),
		);
		if (details.value.projectRole === null) {
			projectsStore.setArchivedAdminProjects([
				...projectsStore.archivedAdminProjects.filter((item) => item.id !== props.id),
				project,
			]);
		} else {
			projectsStore.setArchivedProjects([
				...projectsStore.archivedProjects.filter((item) => item.id !== props.id),
				project,
			]);
		}
		details.value.archived = true;
		isArchiveDialogOpen.value = false;
		await router.push({ name: "landing" });
	} catch {
		errorMessage.value = "Das Projekt konnte nicht archiviert werden.";
	} finally {
		isArchivingProject.value = false;
	}
}

async function submitRestoreProject(): Promise<void> {
	if (isRestoringProject.value || details.value?.archived !== true || !canUpdateDetails.value) {
		return;
	}

	isRestoringProject.value = true;
	errorMessage.value = "";

	try {
		await projectRepository.restoreProject(props.id);
		const projectsStore = useProjectsStore();
		const project = {
			id: props.id,
			name: details.value.name,
			description: details.value.description,
		};
		projectsStore.setArchivedProjects(
			projectsStore.archivedProjects.filter((item) => item.id !== props.id),
		);
		if (details.value.projectRole === null) {
			projectsStore.setArchivedAdminProjects(
				projectsStore.archivedAdminProjects.filter((item) => item.id !== props.id),
			);
			projectsStore.setAdminProjects([...projectsStore.adminProjects, project]);
		} else {
			projectsStore.setProjects([...projectsStore.projects, project]);
		}
		details.value.archived = false;
	} catch {
		errorMessage.value = "Das Projekt konnte nicht wiederhergestellt werden.";
	} finally {
		isRestoringProject.value = false;
	}
}

function updateDeleteDialogOpen(open: boolean): void {
	if (!open && !isDeletingProject.value) {
		isDeleteDialogOpen.value = false;
	}
}

function openDeleteDialog(): void {
	errorMessage.value = "";
	isDeleteDialogOpen.value = true;
}

async function submitDeleteProject(): Promise<void> {
	if (isDeletingProject.value) {
		return;
	}

	isDeletingProject.value = true;
	errorMessage.value = "";

	try {
		await projectRepository.deleteProject(props.id);

		const projectsStore = useProjectsStore();
		projectsStore.setProjects(projectsStore.projects.filter((p) => p.id !== props.id));
		projectsStore.setAdminProjects(
			projectsStore.adminProjects.filter((p) => p.id !== props.id),
		);
		projectsStore.setArchivedProjects(
			projectsStore.archivedProjects.filter((p) => p.id !== props.id),
		);
		projectsStore.setArchivedAdminProjects(
			projectsStore.archivedAdminProjects.filter((p) => p.id !== props.id),
		);

		const activeProjectStore = useActiveProjectStore();
		activeProjectStore.setActiveProject(null);
		activeProjectStore.setProjectDetails(null);
		activeProjectStore.setProjectUsers([]);
		activeProjectStore.setTicketTypes([]);
		activeProjectStore.setTickets([]);

		isDeleteDialogOpen.value = false;
		await router.replace({ name: "projects" });
	} catch {
		errorMessage.value = "Das Projekt konnte nicht gelöscht werden.";
	} finally {
		isDeletingProject.value = false;
	}
}
</script>

<template>
	<main v-if="details != null" class="project-page">
		<Alert
			v-if="details.archived"
			class="border-amber-500/50 bg-amber-500/10 text-amber-950 dark:text-amber-100"
		>
			<HugeiconsIcon :icon="FolderArchiveIcon" />
			<AlertTitle>Projekt archiviert</AlertTitle>
			<AlertDescription>Dieses Projekt ist schreibgeschützt.</AlertDescription>
			<AlertAction v-if="canUpdateDetails">
				<Button
					type="button"
					variant="outline"
					size="sm"
					:disabled="isRestoringProject"
					@click="submitRestoreProject"
				>
					{{ isRestoringProject ? "Wird wiederhergestellt..." : "Wiederherstellen" }}
				</Button>
			</AlertAction>
		</Alert>
		<Alert v-if="errorMessage" variant="destructive">
			<AlertDescription>{{ errorMessage }}</AlertDescription>
		</Alert>

		<Tabs default-value="overview" class="flex-1 mt-2">
			<div class="project-page__tabs-scroll">
				<TabsList>
					<TabsTrigger value="overview" :disabled="isRestoringTicket">
						Übersicht
					</TabsTrigger>
					<TabsTrigger
						v-if="canModifyTickets"
						value="tickets"
						:disabled="isRestoringTicket"
					>
						Tickets
					</TabsTrigger>
					<TabsTrigger value="activities" :disabled="isRestoringTicket">
						Aktivitäten
					</TabsTrigger>
					<TabsTrigger
						v-if="canOpenSettings"
						value="settings"
						:disabled="isRestoringTicket"
					>
						Einstellungen
					</TabsTrigger>
				</TabsList>
			</div>

			<TabsContent value="overview" class="project-page__tab-content">
				<Card>
					<CardHeader>
						<h1 class="cn-font-heading text-2xl font-semibold">{{ details.name }}</h1>
						<CardDescription>
							{{ details.description || "Keine Beschreibung hinterlegt." }}
						</CardDescription>
					</CardHeader>
					<CardContent class="project-page__facts">
						<div>
							<span>Eigene Rolle</span>
							<strong>{{ projectRoleLabel }}</strong>
						</div>
						<div>
							<span>Tickets</span>
							<strong v-if="projectDashboard">
								{{ openTicketCount }} offen /
								{{ projectDashboard.totalTickets }} gesamt
							</strong>
							<strong v-else>{{
								isProjectDashboardLoading ? "Wird geladen..." : "–"
							}}</strong>
						</div>
					</CardContent>
				</Card>

				<DashboardOverview
					:data="projectDashboard"
					:is-loading="isProjectDashboardLoading"
					:error-message="projectDashboardError"
					:show-project-name="false"
				/>
			</TabsContent>

			<TabsContent v-if="canModifyTickets" value="tickets" class="project-page__tab-content">
				<CreateTicketForm
					v-if="isAdmin || details.projectRole !== 'VIEWER'"
					:parent-ticket-id="null"
					:disabled="details.archived"
				/>
				<p v-else>Als Viewer kannst du keine Tickets erstellen.</p>
				<div class="project-page__ticket-filter" role="group" aria-label="Ticketansicht">
					<Button
						type="button"
						:variant="showArchived ? 'outline' : 'default'"
						:aria-pressed="!showArchived"
						:disabled="isLoadingTickets || isRestoringTicket"
						@click="showArchived = false"
					>
						Aktive Tickets
					</Button>
					<Button
						type="button"
						:variant="showArchived ? 'default' : 'outline'"
						:aria-pressed="showArchived"
						:disabled="isLoadingTickets || isRestoringTicket"
						@click="showArchived = true"
					>
						Archivierte Tickets
					</Button>
				</div>
				<p v-if="isLoadingTickets">Tickets werden geladen...</p>
				<div v-else class="project-page__tickets">
					<Card :class="{ 'project-page__mobile-hidden': showMobileTicketDetail }">
						<CardHeader>
							<CardTitle>Tickets</CardTitle>
						</CardHeader>
						<CardContent>
							<TicketList
								:tickets="tickets"
								:ticket-types="ticketTypes"
								:selected-ticket-id="selectedTicketId"
								@select-ticket="selectTicket"
							/>
						</CardContent>
					</Card>

					<Card
						class="project-page__ticket-detail-card"
						:class="{ 'project-page__mobile-hidden': !showMobileTicketDetail }"
					>
						<CardContent>
							<Button
								class="project-page__ticket-back"
								type="button"
								variant="ghost"
								@click="showMobileTicketDetail = false"
							>
								Zurück zur Ticketliste
							</Button>
							<TicketDetail
								v-if="selectedTicket"
								:ticket="selectedTicket"
								:ticket-type="selectedTicketType"
								:parent-ticket-name="selectedTicketParentName"
								:child-tickets="selectedTicketChildren"
								:is-admin="isAdmin"
								@select-ticket="selectTicket"
								@restore-pending="isRestoringTicket = $event"
							/>
							<p v-else>Wähle ein Ticket aus.</p>
						</CardContent>
					</Card>
				</div>
			</TabsContent>

			<TabsContent
				value="activities"
				class="project-page__tab-content min-h-0 overflow-hidden"
			>
				<Card class="min-h-0 flex-1">
					<CardContent class="flex min-h-0 flex-1">
						<ActivityTimeline
							class="min-h-0 flex-1"
							:events="activityEvents"
							:is-loading="isActivityLoading"
							:error-message="activityError"
							fill-height
						/>
					</CardContent>
				</Card>
			</TabsContent>

			<TabsContent v-if="canOpenSettings" value="settings" class="project-page__tab-content">
				<Card v-if="canUpdateDetails">
					<CardHeader>
						<CardTitle>Projektdetails</CardTitle>
					</CardHeader>
					<CardContent>
						<form
							class="project-page__details-form"
							@submit.prevent="submitDetailsUpdate"
						>
							<label>
								Projektname
								<Input
									v-model="draftName"
									required
									:disabled="isUpdatingDetails || !canModifyProject"
								/>
							</label>
							<label>
								Beschreibung
								<Textarea
									v-model="draftDescription"
									:disabled="isUpdatingDetails || !canModifyProject"
								></Textarea>
							</label>
							<Button
								type="submit"
								:disabled="
									isUpdatingDetails || !canModifyProject || !draftName.trim()
								"
							>
								{{
									isUpdatingDetails
										? "Wird gespeichert..."
										: "Änderungen speichern"
								}}
							</Button>
						</form>
					</CardContent>
				</Card>
				<Card>
					<CardContent>
						<ProjectUserManagement :project-id="id" :disabled="details.archived" />
					</CardContent>
				</Card>
				<Card v-if="canUpdateDetails">
					<CardHeader>
						<CardTitle>Projektverwaltung</CardTitle>
					</CardHeader>
					<CardContent class="project-page__management">
						<section>
							<h3>Archivierung</h3>
							<p>
								Archivierte Projekte bleiben erhalten, sind aber schreibgeschützt.
							</p>
							<Button
								v-if="details.archived"
								type="button"
								:disabled="isRestoringProject"
								@click="submitRestoreProject"
							>
								{{
									isRestoringProject
										? "Wird wiederhergestellt..."
										: "Projekt wiederherstellen"
								}}
							</Button>
							<Button
								v-else
								type="button"
								:disabled="isArchivingProject"
								@click="openArchiveDialog"
							>
								Projekt archivieren
							</Button>
						</section>
						<Separator />
						<section>
							<h3>Projekt löschen</h3>
							<p>Das Projekt und alle zugehörigen Daten werden dauerhaft entfernt.</p>
							<Button
								type="button"
								variant="destructive"
								:disabled="isDeletingProject"
								@click="openDeleteDialog"
							>
								Projekt löschen
							</Button>
						</section>
					</CardContent>
				</Card>
			</TabsContent>
		</Tabs>

		<AlertDialog :open="isArchiveDialogOpen" @update:open="updateArchiveDialogOpen">
			<AlertDialogContent>
				<AlertDialogHeader>
					<AlertDialogTitle>Projekt archivieren?</AlertDialogTitle>
					<AlertDialogDescription>
						Das Projekt „{{ details.name }}“ wird archiviert.
					</AlertDialogDescription>
				</AlertDialogHeader>
				<p v-if="errorMessage" class="m-0 text-sm text-destructive" role="alert">
					{{ errorMessage }}
				</p>
				<AlertDialogFooter>
					<AlertDialogCancel :disabled="isArchivingProject">
						Abbrechen
					</AlertDialogCancel>
					<Button :disabled="isArchivingProject" @click="submitArchiveProject">
						{{ isArchivingProject ? "Wird archiviert..." : "Projekt archivieren" }}
					</Button>
				</AlertDialogFooter>
			</AlertDialogContent>
		</AlertDialog>

		<AlertDialog :open="isDeleteDialogOpen" @update:open="updateDeleteDialogOpen">
			<AlertDialogContent>
				<AlertDialogHeader>
					<AlertDialogTitle>Projekt unwiderruflich löschen?</AlertDialogTitle>
					<AlertDialogDescription>
						Das Projekt „{{ details.name }}“ und alle zugehörigen Tickets, Kommentare
						und Aktivitäten werden dauerhaft gelöscht.
					</AlertDialogDescription>
				</AlertDialogHeader>
				<p v-if="errorMessage" class="m-0 text-sm text-destructive" role="alert">
					{{ errorMessage }}
				</p>
				<AlertDialogFooter>
					<AlertDialogCancel :disabled="isDeletingProject"> Abbrechen </AlertDialogCancel>
					<Button
						variant="destructive"
						:disabled="isDeletingProject"
						@click="submitDeleteProject"
					>
						{{ isDeletingProject ? "Wird gelöscht..." : "Projekt löschen" }}
					</Button>
				</AlertDialogFooter>
			</AlertDialogContent>
		</AlertDialog>
	</main>
	<p v-else>Loading...</p>
</template>

<style scoped>
.project-page {
	display: flex;
	height: 100%;
	box-sizing: border-box;
	flex-direction: column;
	width: min(100%, 90rem);
	margin: 0 auto;
	padding: 2rem;
}

.project-page__tabs-scroll {
	overflow-x: auto;
	padding-bottom: 0.25rem;
}

.project-page__facts {
	display: flex;
	flex-wrap: wrap;
	gap: 1rem 2.5rem;
}

.project-page__facts div {
	display: grid;
	gap: 0.125rem;
}

.project-page__facts span {
	color: var(--muted-foreground);
	font-size: 0.75rem;
	text-transform: uppercase;
	letter-spacing: 0.04em;
}

.project-page__tab-content {
	display: flex;
	min-height: 0;
	flex-direction: column;
	gap: 1rem;
	padding-top: 1rem;
}

.project-page__tickets {
	display: grid;
	grid-template-columns: minmax(14rem, 0.7fr) minmax(0, 2fr);
	gap: 1rem;
	align-items: stretch;
}

.project-page__ticket-filter {
	display: flex;
	align-items: center;
	gap: 0.75rem;
}

.project-page__ticket-back {
	display: none;
}

.project-page__management,
.project-page__management section {
	display: grid;
	gap: 0.75rem;
}

.project-page__management h3 {
	font-weight: 600;
}

.project-page__management section > .cn-button,
.project-page__management section > button {
	justify-self: start;
}

.project-page__details-form,
.project-page__details-form label {
	display: flex;
	flex-direction: column;
	gap: 0.5rem;
}

.project-page__details-form {
	align-items: start;
	gap: 1rem;
}

.project-page__details-form label {
	width: min(100%, 40rem);
}

.project-page__details-form textarea {
	padding: 0.45rem 0.6rem;
	border: 1px solid var(--border);
	background: var(--background);
	color: var(--foreground);
	font: inherit;
}

.project-page__details-form textarea {
	width: 100%;
	box-sizing: border-box;
}

.project-page__details-form textarea {
	min-height: 5rem;
	resize: vertical;
}

.project-page h1,
.project-page p {
	margin: 0;
}

.project-page__ticket-detail-card :deep(.ticket-detail) {
	min-height: 0;
	padding: 0;
	border: 0;
}

@media (max-width: 48rem) {
	.project-page {
		padding: 1rem;
	}

	.project-page__tickets {
		grid-template-columns: 1fr;
	}

	.project-page__tickets > .project-page__mobile-hidden {
		display: none;
	}

	.project-page__ticket-back {
		display: inline-flex;
		margin-bottom: 1rem;
	}
}
</style>
