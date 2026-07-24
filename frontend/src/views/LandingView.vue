<script setup lang="ts">
import { DashboardOverview, useGlobalDashboard } from "@/feature/dashboard";

const {
	data: globalDashboard,
	isLoading: isGlobalDashboardLoading,
	errorMessage: globalDashboardError,
} = useGlobalDashboard();
</script>

<template>
	<DashboardOverview
		:data="globalDashboard"
		:is-loading="isGlobalDashboardLoading"
		:error-message="globalDashboardError"
		:show-project-name="true"
	/>
</template>
