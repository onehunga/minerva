<script setup lang="ts">
import { useProject } from "..";
import { CreateTicketForm, TicketList } from "@/feature/ticket";
import ProjectUserManagement from "./ProjectUserManagement.vue";

const { details } = useProject();
</script>

<template>
	<div v-if="details != null" class="project-details">
		<section class="project-section">
			<h1>{{ details.name }}</h1>
			<p>{{ details.description }}</p>
		</section>

		<section class="project-section">
			<h2>Tickets</h2>
			<TicketList />
			<p v-if="details.projectRole === 'VIEWER'">
				Als Viewer kannst du keine Tickets erstellen.
			</p>
			<CreateTicketForm v-else />
		</section>

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
