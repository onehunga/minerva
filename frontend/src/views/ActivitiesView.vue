<script setup lang="ts">
import { ActivityTimeline, useUserActivities, useUserActorActivities } from "@/feature/activity";

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
</script>

<template>
	<main>
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
	</main>
</template>
