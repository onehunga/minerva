<script setup lang="ts">
import { computed } from "vue";
import type {
	DashboardCategoryCount,
	DashboardPriorityRow,
	DashboardRecentTicket,
	DashboardResponse,
} from "../dashboard.model";
import { TICKET_PRIORITY_LABELS, TICKET_PRIORITY_ORDER } from "@/feature/ticket/priority-labels";
import type { TicketPriorityName } from "@/feature/ticket/ticket.model";

const props = defineProps<{
	data: DashboardResponse | null;
	isLoading: boolean;
	errorMessage: string;
	showProjectName: boolean;
}>();

const prioritiesInOrder = computed<DashboardPriorityRow[]>(() => {
	const data = props.data;
	if (data === null) {
		return TICKET_PRIORITY_ORDER.map((priority) => ({
			priority,
			open: 0,
			inProgress: 0,
			completed: 0,
			total: 0,
		}));
	}

	return TICKET_PRIORITY_ORDER.map(
		(priority) =>
			data.priorities.find((row) => row.priority === priority) ?? {
				priority,
				open: 0,
				inProgress: 0,
				completed: 0,
				total: 0,
			},
	);
});

function categoryCount(): DashboardCategoryCount {
	return (
		props.data?.ticketsByCategory ?? {
			open: 0,
			inProgress: 0,
			completed: 0,
		}
	);
}

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

		<template v-else>
			<p>
				Gesamt: <strong>{{ data?.totalTickets ?? 0 }}</strong>
			</p>
			<p>
				Offen: <strong>{{ categoryCount().open }}</strong> | In Arbeit:
				<strong>{{ categoryCount().inProgress }}</strong> | Abgeschlossen:
				<strong>{{ categoryCount().completed }}</strong>
			</p>

			<table>
				<thead>
					<tr>
						<th>Priorität</th>
						<th>Offen</th>
						<th>In Arbeit</th>
						<th>Abgeschlossen</th>
						<th>Gesamt</th>
					</tr>
				</thead>
				<tbody>
					<tr v-for="row in prioritiesInOrder" :key="row.priority">
						<td>{{ priorityLabel(row.priority) }}</td>
						<td>{{ row.open }}</td>
						<td>{{ row.inProgress }}</td>
						<td>{{ row.completed }}</td>
						<td>{{ row.total }}</td>
					</tr>
				</tbody>
			</table>

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
	gap: 0.75rem;
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
</style>
