<script setup lang="ts">
import type { Ticket, TicketType } from "../ticket.model";
import TicketListItem from "./TicketListItem.vue";

defineProps<{
	tickets: Ticket[];
	ticketTypes: TicketType[];
	selectedTicketId: number | null;
	emptyTitle?: string;
	emptyDescription?: string;
}>();

const emit = defineEmits<{
	selectTicket: [ticketId: number];
}>();
</script>

<template>
	<div v-if="tickets.length === 0" class="py-4 text-center text-muted-foreground">
		<p class="font-medium text-foreground">{{ emptyTitle ?? "Keine Tickets vorhanden" }}</p>
		<p class="mt-1 text-sm">
			{{ emptyDescription ?? "Tickets erscheinen hier, sobald sie erstellt wurden." }}
		</p>
	</div>
	<div v-else class="flex flex-col gap-2" aria-label="Tickets">
		<TicketListItem
			v-for="ticket in tickets"
			:key="ticket.id"
			:ticket="ticket"
			:all-tickets="tickets"
			:ticket-types="ticketTypes"
			:selectedTicketId="selectedTicketId"
			@selectTicket="emit('selectTicket', $event)"
		/>
	</div>
</template>

<style scoped></style>
