<script setup lang="ts">
import { ref } from "vue";
import { useRouter } from "vue-router";
import { useProject, useProjectRepository } from "..";
import { ActivityTimeline, useProjectActivities } from "@/feature/activity";
import { DashboardOverview, useProjectDashboard } from "@/feature/dashboard";
import { CreateTicketForm, TicketList } from "@/feature/ticket";
import ProjectUserManagement from "./ProjectUserManagement.vue";

const { details } = useProject();
const projectRepository = useProjectRepository();
const router = useRouter();
const isArchivingProject = ref(false);

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
			<h1>{{ details.name }}</h1>
			<p>{{ details.description }}</p>
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

		<section class="project-section">
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

		<section v-if="details.projectRole === 'OWNER'" class="project-section">
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

.archive-banner {
	margin: 0;
	padding: 1rem;
	border: 2px solid currentColor;
	font-weight: 700;
}
</style>
