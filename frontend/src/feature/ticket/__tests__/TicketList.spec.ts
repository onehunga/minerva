import { mount } from "@vue/test-utils";
import { describe, expect, it } from "vitest";
import type { Ticket } from "../ticket.model";
import TicketList from "../components/TicketList.vue";

const tickets: Ticket[] = [
	{
		id: 1,
		projectId: 1,
		ticketTypeId: 1,
		statusId: 1,
		priority: "NORMAL",
		parentTicketId: null,
		name: "Erstes Ticket",
		description: "",
		children: [],
		createdBy: 1,
		assignedTo: null,
		createdAt: null,
		updatedAt: null,
		archived: false,
	},
	{
		id: 2,
		projectId: 1,
		ticketTypeId: 1,
		statusId: 1,
		priority: "HIGH",
		parentTicketId: null,
		name: "Zweites Ticket",
		description: "",
		children: [],
		createdBy: 1,
		assignedTo: null,
		createdAt: null,
		updatedAt: null,
		archived: false,
	},
];

describe("TicketList", () => {
	it("renders the selection and emits the selected ticket id", async () => {
		const wrapper = mount(TicketList, {
			props: { tickets, selectedTicketId: 1 },
		});

		expect(wrapper.get('[aria-current="true"]').text()).toContain("Erstes Ticket");

		await wrapper.findAll("button")[1]?.trigger("click");

		expect(wrapper.emitted("selectTicket")).toEqual([[2]]);
	});

	it("shows and selects ticket children in the dropdown", async () => {
		const wrapper = mount(TicketList, {
			props: {
				tickets: [
					{
						...tickets[0]!,
						children: [{ id: 3, name: "Unterticket", description: "" }],
					},
				],
				selectedTicketId: null,
			},
		});

		await wrapper.get('[data-slot="collapsible-trigger"]').trigger("click");

		expect(wrapper.text()).toContain("Unterticket");
		expect(wrapper.text()).toContain("#3");

		await wrapper.get('[aria-label="Ticket Unterticket auswählen"]').trigger("click");

		expect(wrapper.emitted("selectTicket")).toEqual([[3]]);
	});
});
