<script setup lang="ts">
import type { Ticket } from "../ticket.model";
import TicketListItem from "./TicketListItem.vue";

defineProps<{
	tickets: Ticket[];
	selectedTicketId: number | null;
}>();

const emit = defineEmits<{
	selectTicket: [ticketId: number];
}>();
</script>

<template>
	<p v-if="tickets.length === 0" class="m-0">Keine Tickets vorhanden.</p>
	<div v-else class="flex flex-col gap-2" aria-label="Tickets">
		<TicketListItem
			v-for="ticket in tickets"
			:key="ticket.id"
			:ticket="ticket"
			:selectedTicketId="selectedTicketId"
			@selectTicket="emit('selectTicket', $event)"
		/>
	</div>
</template>

<style scoped></style>
