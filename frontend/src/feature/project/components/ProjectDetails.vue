<script setup lang="ts">
import { useProject } from "..";
import { ActivityTimeline, useProjectActivities } from "@/feature/activity";
import { DashboardOverview, useProjectDashboard } from "@/feature/dashboard";
import { CreateTicketForm, TicketList } from "@/feature/ticket";
import ProjectUserManagement from "./ProjectUserManagement.vue";

const { details } = useProject();

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
</script>

<template>
	<div v-if="details != null" class="project-details">
		<section class="project-section">
			<h1>{{ details.name }}</h1>
			<p>{{ details.description }}</p>
		</section>

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
</style>
