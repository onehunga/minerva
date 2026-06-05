<script setup lang="ts">
import { computed, ref, watch } from "vue";
import type { Ticket, TicketType, WorkflowState, WorkflowTransition } from "../ticket.model";
import { useProject } from "@/feature/project";

const { updateTicketStatus } = useProject();

const props = defineProps<{
	ticket: Ticket;
	ticketType?: TicketType;
}>();

const selectedTransitionId = ref<number | null>(null);
const isUpdatingStatus = ref(false);
const statusUpdateError = ref<string | null>(null);

const currentStatus = computed<WorkflowState | undefined>(() =>
	props.ticketType?.states.find((state) => state.id === props.ticket.statusId),
);

const availableTransitions = computed<WorkflowTransition[]>(
	() =>
		props.ticketType?.transitions.filter(
			(transition) => transition.fromStateId === props.ticket.statusId,
		) ?? [],
);

const ticketTypeName = computed(
	() => props.ticketType?.name ?? `Ticketart #${props.ticket.ticketTypeId}`,
);

function getStateName(statusId: number): string {
	return (
		props.ticketType?.states.find((state) => state.id === statusId)?.name ??
		`Status #${statusId}`
	);
}

async function submitStatusUpdate(): Promise<void> {
	if (selectedTransitionId.value == null) {
		return;
	}

	const transition = availableTransitions.value.find(
		(currentTransition) => currentTransition.id === selectedTransitionId.value,
	);

	if (transition === undefined) {
		statusUpdateError.value = "Der gewählte Übergang ist nicht mehr verfügbar.";
		return;
	}

	isUpdatingStatus.value = true;
	statusUpdateError.value = null;

	try {
		await updateTicketStatus(props.ticket.id, transition.id, transition.toStateId);
		selectedTransitionId.value = null;
	} catch {
		statusUpdateError.value = "Der Status konnte nicht aktualisiert werden.";
	} finally {
		isUpdatingStatus.value = false;
	}
}

watch(
	() => props.ticket.statusId,
	() => {
		selectedTransitionId.value = null;
		statusUpdateError.value = null;
	},
);

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
	<article class="ticket-detail">
		<header class="ticket-detail__header">
			<p class="ticket-detail__eyebrow">Ticket #{{ ticket.id }}</p>
			<h3>{{ ticket.name }}</h3>
		</header>

		<p class="ticket-detail__description">
			{{ ticket.description || "Keine Beschreibung hinterlegt." }}
		</p>

		<form class="ticket-detail__field" @submit.prevent="submitStatusUpdate">
			<label for="ticket-status">Status</label>
			<select
				id="ticket-status"
				v-model.number="selectedTransitionId"
				aria-label="Statusübergang"
				:disabled="isUpdatingStatus || availableTransitions.length === 0"
			>
				<option :value="null">
					{{ currentStatus?.name ?? `Status #${ticket.statusId}` }}
				</option>
				<option
					v-for="transition in availableTransitions"
					:key="transition.id"
					:value="transition.id"
				>
					{{ transition.name }} → {{ getStateName(transition.toStateId) }}
				</option>
			</select>
			<button type="submit" :disabled="selectedTransitionId == null || isUpdatingStatus">
				{{ isUpdatingStatus ? "Aktualisiere..." : "Status aktualisieren" }}
			</button>
			<p v-if="availableTransitions.length === 0" class="ticket-detail__hint">
				Keine Übergänge verfügbar.
			</p>
			<p v-if="statusUpdateError" class="ticket-detail__error">{{ statusUpdateError }}</p>
		</form>

		<dl class="ticket-detail__meta">
			<div>
				<dt>Ticketart</dt>
				<dd>{{ ticketTypeName }}</dd>
			</div>
			<div>
				<dt>Projekt</dt>
				<dd>#{{ ticket.projectId }}</dd>
			</div>
			<div>
				<dt>Erstellt von</dt>
				<dd>#{{ ticket.createdBy }}</dd>
			</div>
			<div>
				<dt>Zugewiesen an</dt>
				<dd>
					{{ ticket.assignedTo == null ? "Nicht zugewiesen" : `#${ticket.assignedTo}` }}
				</dd>
			</div>
			<div>
				<dt>Erstellt am</dt>
				<dd>{{ formatDate(ticket.createdAt) }}</dd>
			</div>
			<div>
				<dt>Aktualisiert am</dt>
				<dd>{{ formatDate(ticket.updatedAt) }}</dd>
			</div>
		</dl>
	</article>
</template>

<style scoped>
.ticket-detail {
	display: flex;
	flex-direction: column;
	gap: 1rem;
	padding: 1rem;
	border: 1px solid currentColor;
	min-height: 18rem;
	box-sizing: border-box;
}

.ticket-detail__header,
.ticket-detail__description,
.ticket-detail__meta {
	margin: 0;
}

.ticket-detail__header {
	display: flex;
	flex-direction: column;
	gap: 0.25rem;
}

.ticket-detail__header h3,
.ticket-detail__eyebrow {
	margin: 0;
}

.ticket-detail__eyebrow {
	font-size: 0.85rem;
}

.ticket-detail__field {
	display: flex;
	flex-direction: column;
	gap: 0.35rem;
	max-width: 18rem;
}

.ticket-detail__field select,
.ticket-detail__field button {
	padding: 0.45rem 0.55rem;
	border: 1px solid currentColor;
	background: Canvas;
	color: CanvasText;
}

.ticket-detail__field button {
	cursor: pointer;
}

.ticket-detail__field button:disabled {
	cursor: not-allowed;
}

.ticket-detail__hint,
.ticket-detail__error {
	margin: 0;
	font-size: 0.9rem;
}

.ticket-detail__error {
	color: #b00020;
}

.ticket-detail__meta {
	display: grid;
	grid-template-columns: repeat(auto-fit, minmax(10rem, 1fr));
	gap: 0.75rem 1rem;
}

.ticket-detail__meta div {
	display: flex;
	flex-direction: column;
	gap: 0.2rem;
}

.ticket-detail__meta dt {
	font-weight: 700;
}

.ticket-detail__meta dd {
	margin: 0;
}
</style>
