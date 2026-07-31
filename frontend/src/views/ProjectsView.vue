<script setup lang="ts">
import CenteredPageLayout from "@/components/CenteredPageLayout.vue";
import { ProjectList, useProjects } from "@/feature/project";

const { projects } = useProjects();
</script>

<template>
	<CenteredPageLayout>
		<h2>Projekte</h2>
		<ProjectList
			:projects="projects"
			empty-title="Noch keine Projekte"
			empty-description="Erstelle ein Projekt, um Tickets und Abläufe zu verwalten."
			show-create-action
		/>
	</CenteredPageLayout>
</template>
