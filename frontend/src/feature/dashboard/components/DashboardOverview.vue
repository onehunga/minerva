<script setup lang="ts">
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import {
	Table,
	TableBody,
	TableCell,
	TableEmpty,
	TableHead,
	TableHeader,
	TableRow,
} from "@/components/ui/table";
import { formatDate } from "@/lib/date";
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

			<Card>
				<CardHeader>
					<CardTitle>Zuletzt erstellte Tickets</CardTitle>
					<CardDescription>Die fünf neuesten Tickets im Überblick</CardDescription>
				</CardHeader>
				<CardContent>
					<Table>
						<TableHeader>
							<TableRow>
								<TableHead v-if="showProjectName">Projekt</TableHead>
								<TableHead>Ticket</TableHead>
								<TableHead>Status</TableHead>
								<TableHead>Priorität</TableHead>
								<TableHead>Erstellt</TableHead>
							</TableRow>
						</TableHeader>
						<TableBody>
							<TableEmpty
								v-if="recent().length === 0"
								:colspan="showProjectName ? 5 : 4"
							>
								Keine Tickets vorhanden.
							</TableEmpty>
							<TableRow v-for="ticket in recent()" v-else :key="ticket.id">
								<TableCell v-if="showProjectName">
									{{ ticket.projectName ?? "-" }}
								</TableCell>
								<TableCell>
									<div class="flex flex-col">
										<span class="font-medium">{{ ticket.name }}</span>
										<small class="text-muted-foreground"
											>#{{ ticket.id }}</small
										>
									</div>
								</TableCell>
								<TableCell>{{ ticket.statusName }}</TableCell>
								<TableCell>{{ priorityLabel(ticket.priority) }}</TableCell>
								<TableCell>{{ formatDate(ticket.createdAt) }}</TableCell>
							</TableRow>
						</TableBody>
					</Table>
				</CardContent>
			</Card>
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

@media (max-width: 64rem) {
	.dashboard-overview__charts {
		grid-template-columns: minmax(0, 1fr);
	}
}
</style>
