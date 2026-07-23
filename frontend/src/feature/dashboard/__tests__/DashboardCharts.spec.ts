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
};

describe("dashboard charts", () => {
	it("normalizes missing priorities to all five priority groups", () => {
		const wrapper = shallowMount(TicketPriorityDistributionChart, {
			props: {
				priorities: [
					{
						priority: "NORMAL",
						open: 2,
						inProgress: 1,
						completed: 0,
						total: 3,
					},
				],
			},
			global: { stubs },
		});

		const data = wrapper.getComponent({ name: "VisXYContainer" }).props("data");
		expect(data.map((row: { priority: string }) => row.priority)).toEqual(
			TICKET_PRIORITY_ORDER,
		);
		expect(data[0]).toMatchObject({ open: 0, inProgress: 0, completed: 0 });
		expect(data[2]).toMatchObject({ open: 2, inProgress: 1, completed: 0 });
	});

	it("shows an empty state instead of an empty donut", () => {
		const wrapper = shallowMount(TicketStatusDistributionChart, {
			props: { counts: { open: 0, inProgress: 0, completed: 0 } },
			global: { stubs },
		});

		expect(wrapper.text()).toContain("Keine Tickets vorhanden.");
		expect(wrapper.findComponent(VisSingleContainer).exists()).toBe(false);
	});
});
