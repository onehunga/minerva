<script setup lang="ts">
import { useProjectRepository, type model } from "@/feature/project";
import { onMounted, ref } from "vue";

const projectRepository = useProjectRepository();

const projects = ref<model.ProjectRecord[]>([]);

onMounted(async () => {
	projects.value = await projectRepository.getAllProjects();
});
</script>

<template>
	<div>
		<h2>Projects</h2>
		<div>
			<div
				v-for="project in projects"
				:key="project.id"
				class="project-item"
				@click="$router.push({ name: 'project', params: { id: project.id } })"
			>
				<span>{{ project.name }}</span>
			</div>
		</div>
	</div>
</template>

<style scoped>
.project-item {
	padding: 8px;
	border: 1px solid #ccc;
	border-radius: 4px;
	margin-bottom: 8px;
}
</style>
