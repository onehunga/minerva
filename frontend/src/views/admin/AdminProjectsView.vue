<script setup lang="ts">
import CenteredPageLayout from "@/components/CenteredPageLayout.vue";
import { ProjectList, useProjects } from "@/feature/project";

const { adminProjects } = useProjects();
</script>

<template>
	<CenteredPageLayout>
		<h2>Andere Projekte</h2>
		<ProjectList
			:projects="adminProjects"
			empty-title="Keine weiteren Projekte"
			empty-description="Derzeit gibt es keine Projekte außerhalb deiner Mitgliedschaften."
		/>
	</CenteredPageLayout>
</template>
