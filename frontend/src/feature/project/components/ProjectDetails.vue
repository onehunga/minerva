<script setup lang="ts">
import { computed, ref, watch } from "vue";
import { useRouter } from "vue-router";
import { useProject, useProjectRepository } from "..";
import { ActivityTimeline, useProjectActivities } from "@/feature/activity";
import { DashboardOverview, useProjectDashboard } from "@/feature/dashboard";
import { CreateTicketForm, TicketList } from "@/feature/ticket";
import { useUserStore } from "@/feature/user";
import ProjectUserManagement from "./ProjectUserManagement.vue";

const { details, updateProjectDetails } = useProject();
const projectRepository = useProjectRepository();
const router = useRouter();
const userStore = useUserStore();
const isArchivingProject = ref(false);
const isUpdatingDetails = ref(false);
const editingField = ref<"name" | "description" | null>(null);
const draftName = ref("");
const draftDescription = ref("");
const canUpdateDetails = computed(() => details.value?.projectRole === "OWNER");
const isAdmin = computed(() => userStore.userDetails?.role === "ADMIN");

const props = defineProps<{
	id: number;
}>();

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
		if (editingField.value == null) {
			draftName.value = details.value?.name ?? "";
			draftDescription.value = details.value?.description ?? "";
		}
	},
	{ immediate: true },
);

function beginDetailsEdit(field: "name" | "description"): void {
	if (!canUpdateDetails.value || isUpdatingDetails.value || details.value == null) {
		return;
	}

	draftName.value = details.value.name;
	draftDescription.value = details.value.description;
	editingField.value = field;
}

function cancelDetailsEdit(): void {
	draftName.value = details.value?.name ?? "";
	draftDescription.value = details.value?.description ?? "";
	editingField.value = null;
}

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
		editingField.value = null;
		return;
	}

	isUpdatingDetails.value = true;

	try {
		await updateProjectDetails(name, description);
		editingField.value = null;
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
	<div v-if="details != null" class="project-details">
		<section class="project-section">
			<div v-if="editingField === 'name'" class="project-details__inline-edit">
				<input
					v-model="draftName"
					aria-label="Projektname"
					:disabled="isUpdatingDetails"
					@keyup.enter="submitDetailsUpdate"
					@keyup.escape="cancelDetailsEdit"
				/>
				<button
					type="button"
					aria-label="Projektname speichern"
					:disabled="isUpdatingDetails || !draftName.trim()"
					@click="submitDetailsUpdate"
				>
					✓
				</button>
				<button
					type="button"
					aria-label="Projektname bearbeiten abbrechen"
					:disabled="isUpdatingDetails"
					@click="cancelDetailsEdit"
				>
					×
				</button>
			</div>
			<h1 v-else>
				<button
					v-if="canUpdateDetails"
					type="button"
					class="project-details__editable"
					@click="beginDetailsEdit('name')"
				>
					{{ details.name }}
				</button>
				<template v-else>{{ details.name }}</template>
			</h1>
			<div v-if="editingField === 'description'" class="project-details__inline-edit">
				<textarea
					v-model="draftDescription"
					aria-label="Projektbeschreibung"
					:disabled="isUpdatingDetails"
					@keyup.escape="cancelDetailsEdit"
				></textarea>
				<button
					type="button"
					aria-label="Projektbeschreibung speichern"
					:disabled="isUpdatingDetails || !draftName.trim()"
					@click="submitDetailsUpdate"
				>
					✓
				</button>
				<button
					type="button"
					aria-label="Projektbeschreibung bearbeiten abbrechen"
					:disabled="isUpdatingDetails"
					@click="cancelDetailsEdit"
				>
					×
				</button>
			</div>
			<p v-else>
				<button
					v-if="canUpdateDetails"
					type="button"
					class="project-details__editable"
					@click="beginDetailsEdit('description')"
				>
					{{ details.description || "Keine Beschreibung hinterlegt." }}
				</button>
				<template v-else>
					{{ details.description || "Keine Beschreibung hinterlegt." }}
				</template>
			</p>
		</section>
		<p v-if="details.archived" class="archive-banner" role="status">
			Dieses Projekt ist archiviert.
		</p>

		<section class="project-section">
			<h2>Dashboard</h2>
			<DashboardOverview
				:data="projectDashboard"
				:is-loading="isProjectDashboardLoading"
				:error-message="projectDashboardError"
				:show-project-name="false"
			/>
		</section>

		<section v-if="details.projectRole !== null" class="project-section">
			<h2>Tickets</h2>
			<TicketList />
			<p v-if="details.projectRole === 'VIEWER'">
				Als Viewer kannst du keine Tickets erstellen.
			</p>
			<CreateTicketForm v-else :parent-ticket-id="null" />
		</section>

		<ActivityTimeline
			:events="activityEvents"
			:is-loading="isActivityLoading"
			:error-message="activityError"
		/>

		<section v-if="details.projectRole === 'OWNER' || isAdmin" class="project-section">
			<ProjectUserManagement />
		</section>

		<section
			v-if="details.projectRole === 'OWNER' && !details.archived"
			class="project-section"
		>
			<h2>Projekt archivieren</h2>
			<button type="button" :disabled="isArchivingProject" @click="submitArchiveProject">
				{{ isArchivingProject ? "Wird archiviert..." : "Projekt archivieren" }}
			</button>
		</section>
	</div>
	<p v-else>Loading...</p>
</template>

<style scoped>
.project-details {
	display: flex;
	flex-direction: column;
	gap: 1.5rem;
	max-width: 72rem;
	margin: 0 auto;
	padding: 2rem;
}

.project-section {
	display: flex;
	flex-direction: column;
	gap: 1rem;
	padding: 1rem;
	border: 1px solid currentColor;
}

.project-section h1,
.project-section p {
	margin: 0;
}

.project-details__editable {
	display: inline;
	padding: 0;
	border: 0;
	background: transparent;
	color: inherit;
	font: inherit;
	text-align: left;
	cursor: pointer;
}

.project-details__inline-edit {
	display: flex;
	gap: 0.5rem;
	align-items: start;
}

.project-details__inline-edit input,
.project-details__inline-edit textarea {
	flex: 1;
	min-width: 0;
	padding: 0.45rem 0.55rem;
	border: 1px solid currentColor;
	background: Canvas;
	color: CanvasText;
	font: inherit;
}

.project-details__inline-edit textarea {
	min-height: 5rem;
	resize: vertical;
}

.archive-banner {
	margin: 0;
	padding: 1rem;
	border: 2px solid currentColor;
	font-weight: 700;
}
</style>
