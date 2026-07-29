<script setup lang="ts">
import { ProjectList, useProjects } from "@/feature/project";

const { projects } = useProjects();
</script>

<template>
	<main class="mx-auto flex w-full max-w-6xl flex-col gap-4">
		<h2>Projekte</h2>
		<ProjectList :projects="projects" />
	</main>
</template>
