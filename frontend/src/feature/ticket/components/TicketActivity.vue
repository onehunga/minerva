<script setup lang="ts">
import { onMounted, ref, watch } from "vue";
import { useTicketRepository } from "../composables/useTicketRepository";
import type { ActivityEvent, ActivityEventType } from "../ticket.model";

const props = defineProps<{
	projectId: number;
	ticketId: number;
}>();

const ticketRepository = useTicketRepository();

const events = ref<ActivityEvent[]>([]);
const isLoading = ref(false);
const errorMessage = ref("");

const ACTIVITY_TYPE_LABELS: Record<ActivityEventType, string> = {
	TICKET_COMMENT_CREATED: "Kommentar erstellt",
	TICKET_STATUS_CHANGED: "Status geändert",
	TICKET_ASSIGNEE_CHANGED: "Bearbeiter geändert",
};

async function loadActivities(): Promise<void> {
	isLoading.value = true;
	errorMessage.value = "";

	try {
		events.value = await ticketRepository.getTicketActivities(props.projectId, props.ticketId);
	} catch {
		events.value = [];
		errorMessage.value = "Aktivitäten konnten nicht geladen werden.";
	} finally {
		isLoading.value = false;
	}
}

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

onMounted(() => {
	loadActivities();
});

watch(
	() => [props.projectId, props.ticketId],
	() => {
		loadActivities();
	},
);
</script>

<template>
	<section class="ticket-activity" aria-labelledby="ticket-activity-heading">
		<h4 id="ticket-activity-heading">Aktivitäten</h4>

		<p v-if="isLoading">Aktivitäten werden geladen...</p>
		<p v-else-if="errorMessage" role="alert">{{ errorMessage }}</p>
		<p v-else-if="events.length === 0">Keine Aktivitäten vorhanden.</p>
		<ul v-else class="ticket-activity__list">
			<li v-for="event in events" :key="event.id" class="ticket-activity__item">
				<p class="ticket-activity__title">{{ formatType(event.type) }}</p>
				<small>
					{{ event.actorUsername ?? `#${event.actorUserId}` }} -
					{{ formatDate(event.occurredAt) }}
				</small>
				<details class="ticket-activity__payload">
					<summary>Payload</summary>
					<pre>{{ JSON.stringify(event.payload, null, 2) }}</pre>
				</details>
			</li>
		</ul>
	</section>
</template>

<style scoped>
.ticket-activity {
	display: flex;
	flex-direction: column;
	gap: 0.75rem;
}

.ticket-activity h4,
.ticket-activity p {
	margin: 0;
}

.ticket-activity__list {
	display: flex;
	flex-direction: column;
	gap: 0.5rem;
	margin: 0;
	padding: 0;
	list-style: none;
}

.ticket-activity__item {
	display: flex;
	flex-direction: column;
	gap: 0.3rem;
	padding: 0.6rem 0.8rem;
	border: 1px solid currentColor;
}

.ticket-activity__title {
	font-weight: 700;
}

.ticket-activity__payload summary {
	cursor: pointer;
}

.ticket-activity__payload pre {
	margin: 0.4rem 0 0;
	padding: 0.5rem;
	border: 1px solid currentColor;
	white-space: pre-wrap;
	word-break: break-word;
	font-family: monospace;
	font-size: 0.85rem;
}
</style>
