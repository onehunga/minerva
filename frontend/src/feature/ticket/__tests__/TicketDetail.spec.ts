import { shallowMount } from "@vue/test-utils";
import { describe, expect, it, vi } from "vitest";
import type { Ticket, TicketType } from "../ticket.model";
import TicketDetail from "../components/TicketDetail.vue";

vi.mock("@/feature/project", () => ({
	useProject: () => ({
		details: { value: { projectRole: "OWNER" } },
		projectUsers: {
			value: [{ id: 1, username: "admin", projectRole: "OWNER", member: true }],
		},
		deleteTicket: vi.fn<(ticketId: number) => Promise<void>>(),
		archiveTicket: vi.fn<(ticketId: number) => Promise<void>>(),
		updateTicketStatus:
			vi.fn<(ticketId: number, transitionId: number, statusId: number) => Promise<void>>(),
		updateTicketPriority: vi.fn<(ticketId: number, priority: string) => Promise<void>>(),
		updateTicketDetails:
			vi.fn<(ticketId: number, name: string, description: string) => Promise<void>>(),
		updateTicketAssignee:
			vi.fn<(ticketId: number, assignedTo: number | null) => Promise<void>>(),
	}),
}));

vi.mock("@/feature/activity", () => ({
	ActivityTimeline: { template: "<div>Eventlog</div>" },
	useTicketActivities: () => ({
		events: { value: [] },
		isLoading: { value: false },
		errorMessage: { value: "" },
	}),
}));

const ticket: Ticket = {
	id: 7,
	projectId: 2,
	ticketTypeId: 3,
	statusId: 4,
	priority: "NORMAL",
	parentTicketId: null,
	name: "Anmeldung reparieren",
	description: "Die Anmeldung schlägt fehl.",
	createdBy: 1,
	assignedTo: 1,
	createdAt: "2026-07-23T10:00:00Z",
	updatedAt: "2026-07-23T11:00:00Z",
	archived: false,
};

const ticketType: TicketType = {
	id: 3,
	name: "Aufgabe",
	description: "",
	states: [{ id: 4, name: "Offen", category: "OPEN" }],
	transitions: [],
	children: [],
};

describe("TicketDetail", () => {
	it("separates ticket content from actions and metadata", async () => {
		const wrapper = shallowMount(TicketDetail, {
			props: { ticket, ticketType, childTickets: [] },
			global: {
				stubs: {
					Button: { template: "<button><slot /></button>" },
					Tabs: { template: "<div><slot /></div>" },
					TabsList: { template: "<div><slot /></div>" },
					TabsTrigger: { template: "<button><slot /></button>" },
					TabsContent: { template: "<div><slot /></div>" },
					TicketComments: { template: "<div>Kommentare</div>" },
					ActivityTimeline: { template: "<div>Eventlog</div>" },
					CreateTicketForm: { template: "<div>Kindticketformular</div>" },
				},
			},
		});

		expect(wrapper.text()).toContain("Ticket #7");
		expect(wrapper.text()).toContain("Anmeldung reparieren");
		expect(wrapper.text()).toContain("Kommentare");
		expect(wrapper.text()).toContain("Aktivitäten");
		expect(wrapper.find("#ticket-children-heading").exists()).toBe(false);
		expect(wrapper.get("#ticket-actions-heading").text()).toBe("Aktionen");
		expect(wrapper.get("#ticket-meta-heading").text()).toBe("Eckdaten");
		expect(wrapper.text()).not.toContain("Ticketdetails");

		await wrapper.setProps({ ticketType: { ...ticketType, children: [3] } });

		expect(wrapper.text().indexOf("Kindtickets")).toBeLessThan(
			wrapper.text().indexOf("Kommentare"),
		);
	});
});
