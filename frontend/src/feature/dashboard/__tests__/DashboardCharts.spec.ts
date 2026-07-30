import { shallowMount } from "@vue/test-utils";
import { describe, expect, it, vi } from "vitest";
import { VisSingleContainer } from "@unovis/vue";
import TicketPriorityDistributionChart from "../components/TicketPriorityDistributionChart.vue";
import TicketStatusDistributionChart from "../components/TicketStatusDistributionChart.vue";
import { TICKET_PRIORITY_ORDER } from "@/feature/ticket";

vi.mock("@unovis/vue", () => ({
	VisAxis: { template: "<div />" },
	VisCrosshair: { template: "<div />" },
	VisDonut: { template: "<div />" },
	VisGroupedBar: { template: "<div />" },
	VisSingleContainer: {
		name: "VisSingleContainer",
		props: ["data"],
		template: "<div><slot /></div>",
	},
	VisTooltip: { template: "<div />" },
	VisXYContainer: {
		name: "VisXYContainer",
		props: ["data"],
		template: "<div><slot /></div>",
	},
}));

const slotStub = { template: "<div><slot /></div>" };
const stubs = {
	Card: slotStub,
	CardContent: slotStub,
	CardDescription: slotStub,
	CardHeader: slotStub,
	CardTitle: slotStub,
	ChartContainer: slotStub,
	DashboardPanel: slotStub,
};

describe("dashboard charts", () => {
	it("renders all five priority groups without tickets", () => {
		const wrapper = shallowMount(TicketPriorityDistributionChart, {
			props: { priorities: [] },
			global: { stubs },
		});

		const data = wrapper.getComponent({ name: "VisXYContainer" }).props("data");
		expect(data.map((row: { priority: string }) => row.priority)).toEqual(
			TICKET_PRIORITY_ORDER,
		);
		expect(data[0]).toMatchObject({ open: 0, inProgress: 0, completed: 0 });
	});

	it("renders the status visualization without tickets", () => {
		const wrapper = shallowMount(TicketStatusDistributionChart, {
			props: { counts: { open: 0, inProgress: 0, completed: 0 } },
			global: { stubs },
		});

		expect(wrapper.text()).not.toContain("Keine Tickets vorhanden.");
		expect(wrapper.findComponent(VisSingleContainer).exists()).toBe(true);
	});
});
