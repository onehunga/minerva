import type { ChartConfig } from "@/components/ui/chart";

export const dashboardStatusChartConfig = {
	open: { label: "Offen", color: "var(--chart-1)" },
	inProgress: { label: "In Arbeit", color: "var(--chart-2)" },
	completed: { label: "Abgeschlossen", color: "var(--chart-3)" },
} satisfies ChartConfig;

export type DashboardStatusChartCategory = keyof typeof dashboardStatusChartConfig;
