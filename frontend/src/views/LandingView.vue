<script setup lang="ts">
import { DashboardOverview, useGlobalDashboard } from "@/feature/dashboard";
import { useUser } from "@/feature/user";

const { userDetails } = useUser();
const {
	data: globalDashboard,
	isLoading: isGlobalDashboardLoading,
	errorMessage: globalDashboardError,
} = useGlobalDashboard();
</script>

<template>
	<p v-if="!userDetails">Loading...</p>
	<p v-else>{{ userDetails!.username }}</p>

	<DashboardOverview
		:data="globalDashboard"
		:is-loading="isGlobalDashboardLoading"
		:error-message="globalDashboardError"
		:show-project-name="true"
	/>
</template>
