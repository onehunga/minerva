<script setup lang="ts">
import { computed } from "vue";
import { VisAxis, VisGroupedBar, VisXYContainer } from "@unovis/vue";
import type { DashboardPriorityRow } from "../dashboard.model";
import {
	ChartContainer,
	ChartCrosshair,
	ChartLegendContent,
	ChartTooltip,
	ChartTooltipContent,
	componentToString,
} from "@/components/ui/chart";
import { formatTicketPriority, TICKET_PRIORITY_ORDER } from "@/feature/ticket";
import { dashboardStatusChartConfig as config } from "../dashboard-chart.config";
import DashboardPanel from "./DashboardPanel.vue";

const props = defineProps<{
	priorities: DashboardPriorityRow[];
}>();

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
	<DashboardPanel
		title="Prioritäten und Status"
		description="Statusverteilung innerhalb jeder Priorität"
	>
		<ChartContainer :config="config" class="priority-chart__chart">
			<VisXYContainer :data="chartData">
				<VisGroupedBar
					:x="(item: ChartRow) => item.index"
					:y="[
						(item: ChartRow) => item.open,
						(item: ChartRow) => item.inProgress,
						(item: ChartRow) => item.completed,
					]"
					:color="[config.open.color, config.inProgress.color, config.completed.color]"
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
					:color="[config.open.color, config.inProgress.color, config.completed.color]"
				/>
			</VisXYContainer>
			<ChartLegendContent />
		</ChartContainer>
	</DashboardPanel>
</template>

<style scoped>
.priority-chart__chart {
	min-width: 0;
	min-height: 0;
	flex: 1;
}
</style>
