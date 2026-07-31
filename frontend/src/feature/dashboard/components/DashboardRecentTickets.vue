<script setup lang="ts">
import {
	Table,
	TableBody,
	TableCell,
	TableEmpty,
	TableHead,
	TableHeader,
	TableRow,
} from "@/components/ui/table";
import { formatTicketPriority } from "@/feature/ticket";
import { formatDate } from "@/lib/date";
import type { DashboardRecentTicket } from "../dashboard.model";
import DashboardPanel from "./DashboardPanel.vue";

defineProps<{
	tickets: DashboardRecentTicket[];
	showProjectName: boolean;
}>();
</script>

<template>
	<DashboardPanel
		title="Zuletzt erstellte Tickets"
		description="Die fünf neuesten Tickets im Überblick"
	>
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
				<TableEmpty v-if="tickets.length === 0" :colspan="showProjectName ? 5 : 4">
					<div class="py-3 text-center">
						<p class="font-medium text-foreground">Noch keine Tickets vorhanden</p>
						<p class="mt-1 text-sm">Neu erstellte Tickets erscheinen hier.</p>
					</div>
				</TableEmpty>
				<TableRow v-for="ticket in tickets" v-else :key="ticket.id">
					<TableCell v-if="showProjectName">{{ ticket.projectName ?? "-" }}</TableCell>
					<TableCell>
						<div class="flex flex-col">
							<span class="font-medium">{{ ticket.name }}</span>
							<small class="text-muted-foreground">#{{ ticket.id }}</small>
						</div>
					</TableCell>
					<TableCell>{{ ticket.statusName }}</TableCell>
					<TableCell>{{ formatTicketPriority(ticket.priority) }}</TableCell>
					<TableCell>{{ formatDate(ticket.createdAt) }}</TableCell>
				</TableRow>
			</TableBody>
		</Table>
	</DashboardPanel>
</template>
