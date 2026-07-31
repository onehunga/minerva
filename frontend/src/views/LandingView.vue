<script setup lang="ts">
import { DashboardOverview, useGlobalDashboard } from "@/feature/dashboard";
import { useProjects } from "@/feature/project";

const {
	data: globalDashboard,
	isLoading: isGlobalDashboardLoading,
	errorMessage: globalDashboardError,
} = useGlobalDashboard();
const { projects } = useProjects();
</script>

<template>
	<DashboardOverview
		:data="globalDashboard"
		:is-loading="isGlobalDashboardLoading"
		:error-message="globalDashboardError"
		:show-project-name="true"
		:project-count="projects.length"
	/>
</template>
