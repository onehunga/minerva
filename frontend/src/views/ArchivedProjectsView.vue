<script setup lang="ts">
import CenteredPageLayout from "@/components/CenteredPageLayout.vue";
import { ProjectList, useProjects } from "@/feature/project";

const { archivedProjects } = useProjects();
</script>

<template>
	<CenteredPageLayout>
		<h2>Archivierte Projekte</h2>
		<ProjectList
			:projects="archivedProjects"
			empty-title="Keine archivierten Projekte"
			empty-description="Archivierte eigene Projekte werden hier angezeigt."
		/>
	</CenteredPageLayout>
</template>
