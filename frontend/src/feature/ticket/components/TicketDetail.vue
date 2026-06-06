<script setup lang="ts">
import { computed, ref, watch } from "vue";
import type { Ticket, TicketType, WorkflowState, WorkflowTransition } from "../ticket.model";
import { useProject } from "@/feature/project";
import CustomSelect from "@/components/CustomSelect.vue";

const { updateTicketStatus } = useProject();

const props = defineProps<{
	ticket: Ticket;
	ticketType?: TicketType;
}>();

const selectedTransition = ref<WorkflowTransition | null>(null);
const isUpdatingStatus = ref(false);

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

watch(selectedTransition, () => {
	submitStatusUpdate();
});

async function submitStatusUpdate(): Promise<void> {
	if (selectedTransition.value == null) {
		return;
	}

	const transition = availableTransitions.value.find(
		(currentTransition) => currentTransition.id === selectedTransition.value?.id,
	);

	if (transition === undefined) {
		alert("Der gewählte Übergang ist nicht mehr verfügbar.");
		return;
	}

	isUpdatingStatus.value = true;

	try {
		await updateTicketStatus(props.ticket.id, transition.id, transition.toStateId);
		selectedTransition.value = null;
	} catch {
		alert("Der Status konnte nicht aktualisiert werden.");
	} finally {
		isUpdatingStatus.value = false;
	}
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
	<article class="ticket-detail">
		<header class="ticket-detail__header">
			<p class="ticket-detail__eyebrow">Ticket #{{ ticket.id }}</p>
			<h3>{{ ticket.name }}</h3>
		</header>

		<p class="ticket-detail__description">
			{{ ticket.description || "Keine Beschreibung hinterlegt." }}
		</p>

		<label for="ticket-status">Status</label>
		<CustomSelect :options="availableTransitions" v-model="selectedTransition">
			<template #trigger>
				{{ currentStatus?.name }}
			</template>
			<template #option="{ value: transition }">
				{{ transition.name }} → {{ getStateName(transition.toStateId) }}
			</template>
		</CustomSelect>
		<!-- <select
			id="ticket-status"
			v-model="selectedTransitionId"
			:disabled="isUpdatingStatus || availableTransitions.length === 0"
		>
			<option
				v-for="transition in availableTransitions"
				:key="transition.id"
				:value="transition.id"
			></option>
		</select>
 -->
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
