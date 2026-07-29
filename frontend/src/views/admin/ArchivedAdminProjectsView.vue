<script setup lang="ts">
import { ProjectList, useProjects } from "@/feature/project";

const { archivedAdminProjects } = useProjects();
</script>

<template>
	<main class="flex flex-col gap-4">
		<h2>Archivierte Projekte</h2>
		<ProjectList :projects="archivedAdminProjects" />
	</main>
</template>
