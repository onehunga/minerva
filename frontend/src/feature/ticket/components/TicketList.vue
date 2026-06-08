<script setup lang="ts">
import { computed, ref, watch } from "vue";
import TicketDetail from "./TicketDetail.vue";
import { useProject } from "@/feature/project";

const { details: projectDetails, ticketTypes, tickets } = useProject();

const selectedTicketId = ref<number | null>(null);

const selectedTicket = computed(() =>
	tickets.value.find((ticket) => ticket.id === selectedTicketId.value),
);

const selectedTicketType = computed(() =>
	ticketTypes.value.find((ticketType) => ticketType.id === selectedTicket.value?.ticketTypeId),
);

const selectedTicketChildren = computed(() =>
	tickets.value.filter((ticket) => ticket.parentTicketId === selectedTicket.value?.id),
);

function selectTicket(ticketId: number): void {
	selectedTicketId.value = ticketId;
}

watch(
	tickets,
	() => {
		if (!tickets.value.some((ticket) => ticket.id === selectedTicketId.value)) {
			selectedTicketId.value = tickets.value[0]?.id ?? null;
		}
	},
	{ immediate: true },
);
</script>

<template>
	<div class="ticket-list">
		<p v-if="projectDetails == null">Tickets werden geladen...</p>
		<p v-else-if="tickets.length === 0" class="ticket-list__message">
			Für dieses Projekt sind noch keine Tickets angelegt.
		</p>
		<div v-else class="ticket-list__content">
			<aside class="ticket-list__panel" aria-label="Tickets">
				<ul class="ticket-list__items">
					<li v-for="ticket in tickets" :key="ticket.id">
						<button
							type="button"
							class="ticket-list__item"
							:class="{
								'ticket-list__item--selected': selectedTicketId === ticket.id,
							}"
							@click="selectTicket(ticket.id)"
						>
							<span>{{ ticket.name }}</span>
							<small>#{{ ticket.id }}</small>
						</button>
					</li>
				</ul>
			</aside>

			<TicketDetail
				v-if="selectedTicket"
				:ticket="selectedTicket"
				:ticket-type="selectedTicketType"
				:child-tickets="selectedTicketChildren"
				@select-ticket="selectTicket"
			/>
		</div>
	</div>
</template>

<style scoped>
.ticket-list {
	display: flex;
	flex-direction: column;
	gap: 0.75rem;
}

.ticket-list__content {
	display: grid;
	grid-template-columns: minmax(12rem, 18rem) minmax(0, 1fr);
	gap: 1rem;
	align-items: stretch;
}

.ticket-list__panel {
	padding: 1rem;
	border: 1px solid currentColor;
	min-height: 18rem;
	box-sizing: border-box;
}

.ticket-list__items {
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
	text-align: left;
	padding: 0.6rem 0.8rem;
	border: 1px solid currentColor;
	background: transparent;
	color: inherit;
	cursor: pointer;
}

.ticket-list__item--selected {
	font-weight: 700;
	outline: 2px solid currentColor;
	outline-offset: 1px;
}

.ticket-list p,
.ticket-list__message {
	margin: 0;
}

@media (max-width: 48rem) {
	.ticket-list__content {
		grid-template-columns: 1fr;
	}
}
</style>
