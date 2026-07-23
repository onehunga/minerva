<script setup lang="ts">
import { computed, ref, watch } from "vue";
import { useRouter } from "vue-router";
import { ActivityTimeline, useProjectActivities } from "@/feature/activity";
import { DashboardOverview, useProjectDashboard } from "@/feature/dashboard";
import { CreateTicketForm, TicketDetail, TicketList } from "@/feature/ticket";
import { useUserStore } from "@/feature/user";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { useProject, useProjectRepository } from "..";
import ProjectUserManagement from "./ProjectUserManagement.vue";

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
const isArchivingProject = ref(false);
const isUpdatingDetails = ref(false);
const draftName = ref("");
const draftDescription = ref("");

const canUpdateDetails = computed(() => details.value?.projectRole === "OWNER");
const isAdmin = computed(() => userStore.userDetails?.role === "ADMIN");
const canOpenSettings = computed(() => details.value?.projectRole === "OWNER" || isAdmin.value);
const selectedTicket = computed(() =>
	tickets.value.find((ticket) => ticket.id === selectedTicketId.value),
);
const selectedTicketType = computed(() =>
	ticketTypes.value.find((ticketType) => ticketType.id === selectedTicket.value?.ticketTypeId),
);
const selectedTicketChildren = computed(() =>
	tickets.value.filter((ticket) => ticket.parentTicketId === selectedTicket.value?.id),
);

const {
	events: activityEvents,
	isLoading: isActivityLoading,
	errorMessage: activityError,
} = useProjectActivities(props.id);
const {
	data: projectDashboard,
	isLoading: isProjectDashboardLoading,
	errorMessage: projectDashboardError,
} = useProjectDashboard(props.id);

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
			selectedTicketId.value = tickets.value[0]?.id ?? null;
		}
	},
	{ immediate: true },
);

watch(showArchived, async (archived) => {
	isLoadingTickets.value = true;

	try {
		await fetchTickets(archived);
	} catch {
		alert("Die Tickets konnten nicht geladen werden.");
	} finally {
		isLoadingTickets.value = false;
	}
});

async function submitDetailsUpdate(): Promise<void> {
	if (
		isUpdatingDetails.value ||
		!canUpdateDetails.value ||
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

	try {
		await updateProjectDetails(name, description);
	} catch {
		alert("Die Projektdetails konnten nicht aktualisiert werden.");
	} finally {
		isUpdatingDetails.value = false;
	}
}

async function submitArchiveProject(): Promise<void> {
	if (isArchivingProject.value || !confirm("Projekt wirklich archivieren?")) {
		return;
	}

	isArchivingProject.value = true;

	try {
		await projectRepository.archiveProject(props.id);
		await router.push({ name: "landing" });
	} catch {
		alert("Das Projekt konnte nicht archiviert werden.");
	} finally {
		isArchivingProject.value = false;
	}
}
</script>

<template>
	<main v-if="details != null" class="project-page">
		<p v-if="details.archived" class="project-page__archive-banner" role="status">
			Dieses Projekt ist archiviert.
		</p>

		<Tabs default-value="overview">
			<div class="project-page__tabs-scroll">
				<TabsList>
					<TabsTrigger value="overview">Übersicht</TabsTrigger>
					<TabsTrigger v-if="details.projectRole !== null" value="tickets">
						Tickets
					</TabsTrigger>
					<TabsTrigger value="activities">Aktivitäten</TabsTrigger>
					<TabsTrigger v-if="canOpenSettings" value="settings">
						Einstellungen
					</TabsTrigger>
				</TabsList>
			</div>

			<TabsContent value="overview" class="project-page__tab-content">
				<Card>
					<CardContent>
						<h1>{{ details.name }}</h1>
						<p>{{ details.description || "Keine Beschreibung hinterlegt." }}</p>
					</CardContent>
				</Card>

				<Card>
					<CardHeader><CardTitle>Dashboard</CardTitle></CardHeader>
					<CardContent>
						<DashboardOverview
							:data="projectDashboard"
							:is-loading="isProjectDashboardLoading"
							:error-message="projectDashboardError"
							:show-project-name="false"
						/>
					</CardContent>
				</Card>
			</TabsContent>

			<TabsContent
				v-if="details.projectRole !== null"
				value="tickets"
				class="project-page__tab-content"
			>
				<label class="project-page__ticket-filter">
					Ticketansicht
					<select v-model="showArchived" :disabled="isLoadingTickets">
						<option :value="false">Aktive Tickets</option>
						<option :value="true">Archivierte Tickets</option>
					</select>
				</label>
				<p v-if="isLoadingTickets">Tickets werden geladen...</p>
				<div v-else class="project-page__tickets">
					<Card>
						<CardHeader><CardTitle>Tickets</CardTitle></CardHeader>
						<CardContent>
							<TicketList
								:tickets="tickets"
								:selected-ticket-id="selectedTicketId"
								@select-ticket="selectedTicketId = $event"
							/>
						</CardContent>
					</Card>

					<Card class="project-page__ticket-detail-card">
						<CardHeader><CardTitle>Ticketdetails</CardTitle></CardHeader>
						<CardContent>
							<TicketDetail
								v-if="selectedTicket"
								:ticket="selectedTicket"
								:ticket-type="selectedTicketType"
								:child-tickets="selectedTicketChildren"
								@select-ticket="selectedTicketId = $event"
							/>
							<p v-else>Wähle ein Ticket aus.</p>
						</CardContent>
					</Card>
				</div>

				<p v-if="details.projectRole === 'VIEWER'">
					Als Viewer kannst du keine Tickets erstellen.
				</p>
				<CreateTicketForm v-else :parent-ticket-id="null" />
			</TabsContent>

			<TabsContent value="activities" class="project-page__tab-content">
				<Card>
					<CardContent>
						<ActivityTimeline
							:events="activityEvents"
							:is-loading="isActivityLoading"
							:error-message="activityError"
						/>
					</CardContent>
				</Card>
			</TabsContent>

			<TabsContent v-if="canOpenSettings" value="settings" class="project-page__tab-content">
				<Card v-if="canUpdateDetails">
					<CardHeader><CardTitle>Projektdetails</CardTitle></CardHeader>
					<CardContent>
						<form
							class="project-page__details-form"
							@submit.prevent="submitDetailsUpdate"
						>
							<label>
								Projektname
								<Input v-model="draftName" required :disabled="isUpdatingDetails" />
							</label>
							<label>
								Beschreibung
								<textarea
									v-model="draftDescription"
									:disabled="isUpdatingDetails"
								></textarea>
							</label>
							<Button
								type="submit"
								:disabled="isUpdatingDetails || !draftName.trim()"
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
					<CardContent><ProjectUserManagement /></CardContent>
				</Card>
				<Card v-if="details.projectRole === 'OWNER' && !details.archived">
					<CardHeader><CardTitle>Projektverwaltung</CardTitle></CardHeader>
					<CardContent>
						<Button
							type="button"
							:disabled="isArchivingProject"
							@click="submitArchiveProject"
						>
							{{ isArchivingProject ? "Wird archiviert..." : "Projekt archivieren" }}
						</Button>
					</CardContent>
				</Card>
			</TabsContent>
		</Tabs>
	</main>
	<p v-else>Loading...</p>
</template>

<style scoped>
.project-page {
	width: min(100%, 90rem);
	margin: 0 auto;
	padding: 2rem;
}

.project-page__tabs-scroll {
	overflow-x: auto;
	padding-bottom: 0.25rem;
}

.project-page__tab-content {
	display: flex;
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

.project-page__ticket-filter select,
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

.project-page__archive-banner {
	margin: 0 0 1rem;
	padding: 1rem;
	border: 1px solid var(--border);
	border-radius: var(--radius-lg);
	background: var(--accent);
	font-weight: 700;
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
}
</style>
