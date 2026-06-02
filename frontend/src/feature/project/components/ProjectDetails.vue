<script setup lang="ts">
import { onMounted, ref } from "vue";
import { type model, useProjectRepository } from "..";

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
	<div v-if="project != null">
		<h1>{{ project.name }}</h1>
		<p>{{ project.description }}</p>
	</div>
	<p v-else>Loading...</p>
</template>
