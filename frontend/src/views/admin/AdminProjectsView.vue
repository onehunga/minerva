<script setup lang="ts">
import { ProjectList, useProjects } from "@/feature/project";

const { adminProjects } = useProjects();
</script>

<template>
	<main class="mx-auto flex w-full max-w-6xl flex-col gap-4">
		<h2>Andere Projekte</h2>
		<ProjectList :projects="adminProjects" />
	</main>
</template>
