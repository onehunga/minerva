<script setup lang="ts">
import { ActivityTimeline, useUserActivities, useUserActorActivities } from "@/feature/activity";
import { Card, CardContent } from "@/components/ui/card";

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
	<main class="grid grid-cols-1 gap-4 lg:h-full lg:min-h-0 lg:grid-cols-2 lg:overflow-hidden">
		<Card class="lg:min-h-0">
			<CardContent class="flex min-h-0 flex-1">
				<ActivityTimeline
					class="min-h-0 flex-1"
					heading="Letzte Aktivitäten"
					description="Alle Aktivitäten in deinen Projekten"
					:events="userActivityEvents"
					:is-loading="isUserActivityLoading"
					:error-message="userActivityError"
					fill-height
				/>
			</CardContent>
		</Card>

		<Card class="lg:min-h-0">
			<CardContent class="flex min-h-0 flex-1">
				<ActivityTimeline
					class="min-h-0 flex-1"
					heading="Meine Aktivitäten"
					:events="userActorActivityEvents"
					:is-loading="isUserActorActivityLoading"
					:error-message="userActorActivityError"
					fill-height
				/>
			</CardContent>
		</Card>
	</main>
</template>
