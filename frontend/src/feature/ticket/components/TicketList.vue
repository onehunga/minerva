<script setup lang="ts">
import type { Ticket } from "../ticket.model";

defineProps<{
	tickets: Ticket[];
	selectedTicketId: number | null;
}>();

const emit = defineEmits<{
	selectTicket: [ticketId: number];
}>();
</script>

<template>
	<p v-if="tickets.length === 0" class="ticket-list__message">Keine Tickets vorhanden.</p>
	<ul v-else class="ticket-list" aria-label="Tickets">
		<li v-for="ticket in tickets" :key="ticket.id">
			<button
				type="button"
				class="ticket-list__item"
				:class="{ 'ticket-list__item--selected': selectedTicketId === ticket.id }"
				:aria-current="selectedTicketId === ticket.id ? 'true' : undefined"
				@click="emit('selectTicket', ticket.id)"
			>
				<span>{{ ticket.name }}</span>
				<small>#{{ ticket.id }}</small>
			</button>
		</li>
	</ul>
</template>

<style scoped>
.ticket-list {
	display: flex;
	flex-direction: column;
	gap: 0.5rem;
	margin: 0;
	padding: 0;
	list-style: none;
}

.ticket-list__item {
	display: flex;
	width: 100%;
	justify-content: space-between;
	gap: 0.75rem;
	padding: 0.6rem 0.8rem;
	border: 1px solid var(--border);
	border-radius: var(--radius-sm);
	background: transparent;
	color: inherit;
	text-align: left;
	cursor: pointer;
}

.ticket-list__item--selected {
	border-color: var(--primary);
	background: var(--accent);
	font-weight: 700;
}

.ticket-list__message {
	margin: 0;
}
</style>
