<script setup lang="ts">
import type { ActivityEvent, ActivityEventType } from "../activity.model";

defineProps<{
	events: ActivityEvent[];
	isLoading: boolean;
	errorMessage: string;
}>();

const ACTIVITY_TYPE_LABELS: Record<ActivityEventType, string> = {
	TICKET_COMMENT_CREATED: "Kommentar erstellt",
	TICKET_STATUS_CHANGED: "Status geändert",
	TICKET_ASSIGNEE_CHANGED: "Bearbeiter geändert",
};

function formatType(type: ActivityEventType): string {
	return ACTIVITY_TYPE_LABELS[type] ?? type;
}

function formatDate(value: string | null): string {
	if (value == null) {
		return "-";
	}

	return new Intl.DateTimeFormat("de-DE", {
		dateStyle: "medium",
		timeStyle: "short",
	}).format(new Date(value));
}
</script>

<template>
	<section class="activity-timeline" aria-labelledby="activity-timeline-heading">
		<h4 id="activity-timeline-heading">Aktivitäten</h4>

		<p v-if="isLoading">Aktivitäten werden geladen...</p>
		<p v-else-if="errorMessage" role="alert">{{ errorMessage }}</p>
		<p v-else-if="events.length === 0">Keine Aktivitäten vorhanden.</p>
		<ul v-else class="activity-timeline__list">
			<li v-for="event in events" :key="event.id" class="activity-timeline__item">
				<p class="activity-timeline__title">{{ formatType(event.type) }}</p>
				<small>
					{{ event.actorUsername ?? `#${event.actorUserId}` }} -
					{{ formatDate(event.occurredAt) }}
				</small>
				<details class="activity-timeline__payload">
					<summary>Payload</summary>
					<pre>{{ JSON.stringify(event.payload, null, 2) }}</pre>
				</details>
			</li>
		</ul>
	</section>
</template>

<style scoped>
.activity-timeline {
	display: flex;
	flex-direction: column;
	gap: 0.75rem;
}

.activity-timeline h4,
.activity-timeline p {
	margin: 0;
}

.activity-timeline__list {
	display: flex;
	flex-direction: column;
	gap: 0.5rem;
	margin: 0;
	padding: 0;
	list-style: none;
}

.activity-timeline__item {
	display: flex;
	flex-direction: column;
	gap: 0.3rem;
	padding: 0.6rem 0.8rem;
	border: 1px solid currentColor;
}

.activity-timeline__title {
	font-weight: 700;
}

.activity-timeline__payload summary {
	cursor: pointer;
}

.activity-timeline__payload pre {
	margin: 0.4rem 0 0;
	padding: 0.5rem;
	border: 1px solid currentColor;
	white-space: pre-wrap;
	word-break: break-word;
	font-family: monospace;
	font-size: 0.85rem;
}
</style>
