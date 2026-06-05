<script setup lang="ts">
import { onMounted, ref } from "vue";
import { type model, useProjectRepository } from "..";
import { CreateTicketForm } from "@/feature/ticket";
import ProjectUserManagement from "./ProjectUserManagement.vue";

const repository = useProjectRepository();

const props = defineProps<{
	id: number;
}>();

const project = ref<model.ProjectDetails | null>(null);

onMounted(async () => {
	project.value = await repository.getProjectDetails(props.id);
});
</script>

<template>
	<div v-if="project != null" class="project-details">
		<section class="project-section">
			<h1>{{ project.name }}</h1>
			<p>{{ project.description }}</p>
		</section>

		<section class="project-section">
			<h2>Tickets</h2>
			<p v-if="project.projectRole === 'VIEWER'">
				Als Viewer kannst du keine Tickets erstellen.
			</p>
			<CreateTicketForm v-else :project-id="project.id" />
		</section>

		<section v-if="project.projectRole === 'OWNER'" class="project-section">
			<ProjectUserManagement :project-id="project.id" />
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
