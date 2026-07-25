<script setup lang="ts">
import { computed } from "vue";
import { VisDonut, VisSingleContainer } from "@unovis/vue";
import type { DashboardCategoryCount } from "../dashboard.model";
import type { ChartConfig } from "@/components/ui/chart";
import { ChartContainer } from "@/components/ui/chart";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";

const props = defineProps<{
	counts: DashboardCategoryCount;
}>();

const config = {
	open: { label: "Offen", color: "var(--chart-1)" },
	inProgress: { label: "In Arbeit", color: "var(--chart-2)" },
	completed: { label: "Abgeschlossen", color: "var(--chart-3)" },
} satisfies ChartConfig;

type StatusCategory = keyof typeof config;
type ChartRow = { category: StatusCategory; count: number; color: string };

const chartData = computed<ChartRow[]>(() => [
	{ category: "open", count: props.counts.open, color: config.open.color },
	{
		category: "inProgress",
		count: props.counts.inProgress,
		color: config.inProgress.color,
	},
	{
		category: "completed",
		count: props.counts.completed,
		color: config.completed.color,
	},
]);
const total = computed(() => chartData.value.reduce((sum, item) => sum + item.count, 0));
</script>

<template>
	<Card>
		<CardHeader>
			<CardTitle>Statusverteilung</CardTitle>
			<CardDescription>Aktive Tickets nach Bearbeitungsstand</CardDescription>
		</CardHeader>
		<CardContent>
			<div class="status-chart__visual">
				<ChartContainer
					:config="config"
					class="status-chart__chart"
					:aria-label="`${total} Tickets nach Status verteilt`"
				>
					<VisSingleContainer :data="chartData">
						<VisDonut
							:value="(item: ChartRow) => item.count"
							:color="(item: ChartRow) => item.color"
							:arc-width="32"
						/>
					</VisSingleContainer>
				</ChartContainer>
				<div class="status-chart__total" aria-hidden="true">
					<strong>{{ total }}</strong>
					<span>Tickets</span>
				</div>
			</div>

			<ul class="status-chart__legend" aria-label="Statuswerte">
				<li v-for="item in chartData" :key="item.category">
					<span class="status-chart__dot" :style="{ backgroundColor: item.color }"></span>
					<span>{{ config[item.category]?.label }}</span>
					<strong>{{ item.count }}</strong>
				</li>
			</ul>
		</CardContent>
	</Card>
</template>

<style scoped>
.status-chart__visual {
	position: relative;
	width: min(100%, 18rem);
	margin: 0 auto;
}

.status-chart__chart {
	height: 15rem;
}

.status-chart__total {
	position: absolute;
	inset: 50% auto auto 50%;
	display: flex;
	transform: translate(-50%, -50%);
	flex-direction: column;
	align-items: center;
}

.status-chart__total strong {
	font-size: 1.5rem;
}

.status-chart__total span {
	color: var(--muted-foreground);
	font-size: 0.75rem;
}

.status-chart__legend {
	display: grid;
	gap: 0.5rem;
	margin: 1rem 0 0;
	padding: 0;
	list-style: none;
}

.status-chart__legend li {
	display: grid;
	grid-template-columns: auto 1fr auto;
	gap: 0.5rem;
	align-items: center;
}

.status-chart__dot {
	width: 0.5rem;
	height: 0.5rem;
	border-radius: 9999px;
}
</style>
