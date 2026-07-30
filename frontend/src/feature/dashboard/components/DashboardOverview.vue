<script setup lang="ts">
import { Alert, AlertDescription } from "@/components/ui/alert";
import { computed } from "vue";
import type { DashboardResponse } from "../dashboard.model";
import DashboardRecentTickets from "./DashboardRecentTickets.vue";
import TicketPriorityDistributionChart from "./TicketPriorityDistributionChart.vue";
import TicketStatusDistributionChart from "./TicketStatusDistributionChart.vue";

const props = defineProps<{
	data: DashboardResponse | null;
	isLoading: boolean;
	errorMessage: string;
	showProjectName: boolean;
	projectCount?: number;
}>();

const openTicketCount = computed(
	() =>
		(props.data?.ticketsByCategory.open ?? 0) + (props.data?.ticketsByCategory.inProgress ?? 0),
);
</script>

<template>
	<section class="dashboard-overview">
		<h3>Dashboard</h3>

		<p v-if="isLoading">Dashboard wird geladen...</p>
		<Alert v-else-if="errorMessage" variant="destructive">
			<AlertDescription>{{ errorMessage }}</AlertDescription>
		</Alert>

		<template v-else-if="data != null">
			<p class="dashboard-overview__summary">
				<template v-if="projectCount != null">
					<strong>{{ projectCount }}</strong>
					{{ projectCount === 1 ? "Projekt" : "Projekte" }},
				</template>
				<strong>{{ openTicketCount }}</strong>
				{{ openTicketCount === 1 ? "offenes Ticket" : "offene Tickets" }} und
				<strong>{{ data.totalTickets }}</strong> insgesamt
			</p>

			<div class="dashboard-overview__charts">
				<TicketStatusDistributionChart :counts="data.ticketsByCategory" />
				<TicketPriorityDistributionChart :priorities="data.priorities" />
			</div>

			<DashboardRecentTickets
				:tickets="data.recentTickets"
				:show-project-name="showProjectName"
			/>
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

.dashboard-overview__charts > * {
	min-width: 0;
}

.dashboard-overview__summary {
	margin: 0;
	padding: 0.75rem 1rem;
	border: 1px solid var(--border);
	border-radius: var(--radius);
	background: var(--muted);
	color: var(--muted-foreground);
}

.dashboard-overview__summary strong {
	color: var(--foreground);
}

@media (max-width: 64rem) {
	.dashboard-overview__charts {
		grid-template-columns: minmax(0, 1fr);
	}
}
</style>
