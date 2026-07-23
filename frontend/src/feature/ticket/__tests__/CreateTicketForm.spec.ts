import { shallowMount } from "@vue/test-utils";
import { describe, expect, it, vi } from "vitest";
import CreateTicketForm from "../components/CreateTicketForm.vue";

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
		tickets: { value: [] },
		createTicket: vi.fn<() => Promise<void>>(),
	}),
}));

const slotStub = { template: "<div><slot /></div>" };

describe("CreateTicketForm", () => {
	it("renders the drawer form with default type and open state", () => {
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
					SelectItem: slotStub,
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
		).toEqual(["10", "100"]);
	});
});
