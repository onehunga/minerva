<script setup lang="ts">
import CenteredPageLayout from "@/components/CenteredPageLayout.vue";
import { ProjectList, useProjects } from "@/feature/project";

const { archivedAdminProjects } = useProjects();
</script>

<template>
	<CenteredPageLayout>
		<h2>Archivierte Projekte</h2>
		<ProjectList :projects="archivedAdminProjects" />
	</CenteredPageLayout>
</template>
