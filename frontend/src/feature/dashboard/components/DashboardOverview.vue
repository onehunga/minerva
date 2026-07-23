<script setup lang="ts">
import type { DashboardRecentTicket, DashboardResponse } from "../dashboard.model";
import TicketPriorityDistributionChart from "./TicketPriorityDistributionChart.vue";
import TicketStatusDistributionChart from "./TicketStatusDistributionChart.vue";
import { TICKET_PRIORITY_LABELS } from "@/feature/ticket/priority-labels";
import type { TicketPriorityName } from "@/feature/ticket/ticket.model";

const props = defineProps<{
	data: DashboardResponse | null;
	isLoading: boolean;
	errorMessage: string;
	showProjectName: boolean;
}>();

function recent(): DashboardRecentTicket[] {
	return props.data?.recentTickets ?? [];
}

function priorityLabel(priority: TicketPriorityName): string {
	return TICKET_PRIORITY_LABELS[priority];
}

function formatDate(value: string | null): string {
	if (value === null) {
		return "-";
	}

	return new Intl.DateTimeFormat("de-DE", {
		dateStyle: "medium",
		timeStyle: "short",
	}).format(new Date(value));
}
</script>

<template>
	<section class="dashboard-overview">
		<h3>Dashboard</h3>

		<p v-if="isLoading">Dashboard wird geladen...</p>
		<p v-else-if="errorMessage" role="alert">{{ errorMessage }}</p>

		<template v-else-if="data != null">
			<div class="dashboard-overview__charts">
				<TicketStatusDistributionChart :counts="data.ticketsByCategory" />
				<TicketPriorityDistributionChart :priorities="data.priorities" />
			</div>

			<h4>Zuletzt erstellte Tickets</h4>
			<p v-if="recent().length === 0">Keine Tickets vorhanden.</p>
			<table v-else>
				<thead>
					<tr>
						<th v-if="showProjectName">Projekt</th>
						<th>Ticket</th>
						<th>Status</th>
						<th>Priorität</th>
						<th>Erstellt</th>
					</tr>
				</thead>
				<tbody>
					<tr v-for="ticket in recent()" :key="ticket.id">
						<td v-if="showProjectName">{{ ticket.projectName ?? "-" }}</td>
						<td>{{ ticket.name }}</td>
						<td>{{ ticket.statusName }}</td>
						<td>{{ priorityLabel(ticket.priority) }}</td>
						<td>{{ formatDate(ticket.createdAt) }}</td>
					</tr>
				</tbody>
			</table>
		</template>
	</section>
</template>

<style scoped>
.dashboard-overview {
	display: flex;
	flex-direction: column;
	gap: 1rem;
}

.dashboard-overview__charts {
	display: grid;
	grid-template-columns: repeat(2, minmax(0, 1fr));
	gap: 1rem;
}

.dashboard-overview table {
	border-collapse: collapse;
}

.dashboard-overview th,
.dashboard-overview td {
	border: 1px solid currentColor;
	padding: 0.4rem 0.6rem;
	text-align: left;
}

@media (max-width: 64rem) {
	.dashboard-overview__charts {
		grid-template-columns: minmax(0, 1fr);
	}
}
</style>
