<script setup lang="ts">
import BaseModal from "@/components/BaseModal.vue";
import { ActivityTimeline, useUserActivities, useUserActorActivities } from "@/feature/activity";
import { DashboardOverview, useGlobalDashboard } from "@/feature/dashboard";
import { CreateProjectForm, ProjectList } from "@/feature/project";
import { useUser } from "@/feature/user";
import { ref } from "vue";

const { userDetails } = useUser();
const {
	events: userActivityEvents,
	isLoading: isUserActivityLoading,
	errorMessage: userActivityError,
} = useUserActivities();
const {
	events: userActorActivityEvents,
	isLoading: isUserActorActivityLoading,
	errorMessage: userActorActivityError,
} = useUserActorActivities();
const {
	data: globalDashboard,
	isLoading: isGlobalDashboardLoading,
	errorMessage: globalDashboardError,
} = useGlobalDashboard();

const showCreateProjectForm = ref(false);
</script>

<template>
	<p v-if="!userDetails">Loading...</p>
	<p v-else>{{ userDetails!.username }}</p>

	<button @click="showCreateProjectForm = true">Create Project</button>

	<BaseModal
		:open="showCreateProjectForm"
		@close="showCreateProjectForm = false"
		title="Create Project"
	>
		<CreateProjectForm />
	</BaseModal>

	<DashboardOverview
		:data="globalDashboard"
		:is-loading="isGlobalDashboardLoading"
		:error-message="globalDashboardError"
		:show-project-name="true"
	/>

	<ActivityTimeline
		heading="Letzte Aktivitäten"
		:events="userActivityEvents"
		:is-loading="isUserActivityLoading"
		:error-message="userActivityError"
	/>

	<ActivityTimeline
		heading="Meine Aktivitäten"
		:events="userActorActivityEvents"
		:is-loading="isUserActorActivityLoading"
		:error-message="userActorActivityError"
	/>

	<ProjectList />
</template>
