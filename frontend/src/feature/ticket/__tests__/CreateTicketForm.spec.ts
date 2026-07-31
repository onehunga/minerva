import { flushPromises, shallowMount } from "@vue/test-utils";
import { describe, expect, it, vi } from "vitest";
import CreateTicketForm from "../components/CreateTicketForm.vue";

const createTicket = vi.hoisted(() => vi.fn<() => Promise<void>>());

vi.mock("@/feature/project", () => ({
	useProject: () => ({
		details: { value: { id: 1, projectRole: "OWNER" } },
		ticketTypes: {
			value: [
				{
					id: 10,
					name: "Aufgabe",
					description: "",
					states: [
						{ id: 100, name: "Offen", category: "OPEN" },
						{ id: 101, name: "Erledigt", category: "COMPLETED" },
					],
					transitions: [],
					children: [],
				},
			],
		},
		projectUsers: {
			value: [
				{ id: 1, username: "owner", projectRole: "OWNER" },
				{ id: 2, username: "viewer", projectRole: "VIEWER" },
			],
		},
		tickets: { value: [] },
		createTicket,
	}),
}));

const slotStub = { template: "<div><slot /></div>" };

describe("CreateTicketForm", () => {
	it("creates a ticket with default priority and no assignee", async () => {
		const wrapper = shallowMount(CreateTicketForm, {
			props: { parentTicketId: null },
			global: {
				stubs: {
					Sheet: slotStub,
					SheetTrigger: slotStub,
					SheetContent: slotStub,
					SheetHeader: slotStub,
					SheetTitle: slotStub,
					SheetDescription: slotStub,
					SheetFooter: slotStub,
					Button: { template: "<button><slot /></button>" },
					Label: { template: "<label><slot /></label>" },
					Select: {
						props: ["modelValue"],
						template:
							'<div class="select-stub" :data-value="modelValue"><slot /></div>',
					},
					SelectContent: slotStub,
					SelectGroup: slotStub,
					SelectItem: slotStub,
					SelectLabel: slotStub,
					SelectTrigger: slotStub,
					SelectValue: slotStub,
				},
			},
		});

		expect(wrapper.get("button").text()).toBe("Ticket erstellen");
		expect(wrapper.text()).toContain("Titel");
		expect(wrapper.text()).toContain("Beschreibung");
		expect(wrapper.get(".create-ticket-form__selects").text()).toContain("Ticketart");
		expect(wrapper.get(".create-ticket-form__selects").text()).toContain("Startzustand");
		expect(
			wrapper.findAll(".select-stub").map((select) => select.attributes("data-value")),
		).toEqual(["10", "100", "NORMAL", "unassigned"]);
		expect(wrapper.text()).toContain("Priorität");
		expect(wrapper.text()).toContain("Bearbeiter");
		expect(wrapper.text()).toContain("owner");
		expect(wrapper.text()).not.toContain("viewer");

		wrapper.findComponent({ name: "Input" }).vm.$emit("update:modelValue", "Ticket");
		await wrapper.get("form").trigger("submit");
		await flushPromises();

		expect(createTicket).toHaveBeenCalledWith({
			name: "Ticket",
			description: "",
			ticketTypeId: 10,
			statusId: 100,
			parentTicketId: null,
			priority: "NORMAL",
			assignedTo: null,
		});
	});
});
