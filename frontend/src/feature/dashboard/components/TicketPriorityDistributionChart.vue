<script setup lang="ts">
import { computed } from "vue";
import { VisAxis, VisGroupedBar, VisXYContainer } from "@unovis/vue";
import type { DashboardPriorityRow } from "../dashboard.model";
import type { ChartConfig } from "@/components/ui/chart";
import {
	ChartContainer,
	ChartCrosshair,
	ChartLegendContent,
	ChartTooltip,
	ChartTooltipContent,
	componentToString,
} from "@/components/ui/chart";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { formatTicketPriority, TICKET_PRIORITY_ORDER } from "@/feature/ticket";

const props = defineProps<{
	priorities: DashboardPriorityRow[];
}>();

const config = {
	open: { label: "Offen", color: "var(--chart-1)" },
	inProgress: { label: "In Arbeit", color: "var(--chart-2)" },
	completed: { label: "Abgeschlossen", color: "var(--chart-3)" },
} satisfies ChartConfig;

const chartData = computed(() =>
	TICKET_PRIORITY_ORDER.map((priority, index) => {
		const row = props.priorities.find((item) => item.priority === priority);
		return {
			index,
			priority,
			open: row?.open ?? 0,
			inProgress: row?.inProgress ?? 0,
			completed: row?.completed ?? 0,
		};
	}),
);

type ChartRow = (typeof chartData.value)[number];

function priorityLabel(value: number | Date): string {
	const priority = chartData.value[Number(value)]?.priority;
	return priority == null ? "" : formatTicketPriority(priority);
}
</script>

<template>
	<Card>
		<CardHeader>
			<CardTitle>Prioritäten und Status</CardTitle>
			<CardDescription>Statusverteilung innerhalb jeder Priorität</CardDescription>
		</CardHeader>
		<CardContent>
			<ChartContainer :config="config" class="priority-chart__chart">
				<VisXYContainer :data="chartData">
					<VisGroupedBar
						:x="(item: ChartRow) => item.index"
						:y="[
							(item: ChartRow) => item.open,
							(item: ChartRow) => item.inProgress,
							(item: ChartRow) => item.completed,
						]"
						:color="[
							config.open.color,
							config.inProgress.color,
							config.completed.color,
						]"
						:rounded-corners="4"
						:group-padding="0.15"
					/>
					<VisAxis
						type="x"
						:x="(item: ChartRow) => item.index"
						:tick-values="chartData.map((item) => item.index)"
						:tick-format="priorityLabel"
						:tick-line="false"
						:domain-line="false"
						:grid-line="false"
					/>
					<VisAxis type="y" :tick-line="false" :domain-line="false" :grid-line="true" />
					<ChartTooltip />
					<ChartCrosshair
						:circle-radius="0"
						:template="
							componentToString(config, ChartTooltipContent, {
								labelFormatter: priorityLabel,
							})
						"
						:color="[
							config.open.color,
							config.inProgress.color,
							config.completed.color,
						]"
					/>
				</VisXYContainer>
				<ChartLegendContent />
			</ChartContainer>
		</CardContent>
	</Card>
</template>

<style scoped>
.priority-chart__chart {
	height: 20rem;
}
</style>
