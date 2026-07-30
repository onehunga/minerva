import { mount } from "@vue/test-utils";
import { describe, expect, it, vi } from "vitest";
import type { DashboardResponse } from "../dashboard.model";
import DashboardOverview from "../components/DashboardOverview.vue";
import TicketPriorityDistributionChart from "../components/TicketPriorityDistributionChart.vue";
import TicketStatusDistributionChart from "../components/TicketStatusDistributionChart.vue";

vi.mock("@unovis/vue", () => ({
	VisAxis: { template: "<div />" },
	VisCrosshair: { template: "<div />" },
	VisDonut: { template: "<div />" },
	VisGroupedBar: { template: "<div />" },
	VisSingleContainer: { template: "<div />" },
	VisTooltip: { template: "<div />" },
	VisXYContainer: { template: "<div />" },
}));

const dashboard: DashboardResponse = {
	totalTickets: 4,
	ticketsByCategory: { open: 2, inProgress: 1, completed: 1 },
	priorities: [
		{
			priority: "NORMAL",
			open: 2,
			inProgress: 1,
			completed: 1,
			total: 4,
		},
	],
	recentTickets: [
		{
			id: 5,
			projectId: 1,
			projectName: "Minerva",
			name: "Dashboard bauen",
			priority: "NORMAL",
			statusName: "Offen",
			statusCategory: "OPEN",
			createdAt: "2026-07-23T10:00:00Z",
		},
	],
};

describe("DashboardOverview", () => {
	it("passes dashboard data to both charts and keeps recent tickets", () => {
		const wrapper = mount(DashboardOverview, {
			props: {
				data: dashboard,
				isLoading: false,
				errorMessage: "",
				showProjectName: true,
				projectCount: 3,
			},
		});

		expect(wrapper.getComponent(TicketStatusDistributionChart).props("counts")).toEqual(
			dashboard.ticketsByCategory,
		);
		expect(wrapper.getComponent(TicketPriorityDistributionChart).props("priorities")).toEqual(
			dashboard.priorities,
		);
		expect(wrapper.text()).toContain("Dashboard bauen");
		expect(wrapper.text()).toContain("#5");
		expect(wrapper.text()).toContain("Minerva");
		expect(wrapper.text()).toContain("3 Projekte, 3 offene Tickets und 4 insgesamt");
	});

	it("hides the project name in a project dashboard", () => {
		const wrapper = mount(DashboardOverview, {
			props: {
				data: dashboard,
				isLoading: false,
				errorMessage: "",
				showProjectName: false,
			},
		});

		expect(wrapper.text()).not.toContain("Projekt");
		expect(wrapper.text()).not.toContain("Minerva");
	});

	it("shows an empty table state without recent tickets", () => {
		const wrapper = mount(DashboardOverview, {
			props: {
				data: { ...dashboard, recentTickets: [] },
				isLoading: false,
				errorMessage: "",
				showProjectName: true,
			},
		});

		expect(wrapper.text()).toContain("Keine Tickets vorhanden.");
	});
});
