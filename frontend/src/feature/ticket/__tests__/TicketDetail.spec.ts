import { enableAutoUnmount, flushPromises, mount, shallowMount } from "@vue/test-utils";
import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";
import { nextTick } from "vue";
import type { Ticket, TicketType } from "../ticket.model";
import TicketDetail from "../components/TicketDetail.vue";

const projectActions = vi.hoisted(() => ({
	deleteTicket: vi.fn<(ticketId: number) => Promise<void>>(),
	archiveTicket: vi.fn<(ticketId: number) => Promise<void>>(),
	restoreTicket: vi.fn<(ticketId: number) => Promise<void>>(),
	updateTicketStatus:
		vi.fn<(ticketId: number, transitionId: number, statusId: number) => Promise<void>>(),
	updateTicketPriority: vi.fn<(ticketId: number, priority: string) => Promise<void>>(),
	updateTicketDetails:
		vi.fn<(ticketId: number, name: string, description: string) => Promise<void>>(),
	updateTicketAssignee: vi.fn<(ticketId: number, assignedTo: number | null) => Promise<void>>(),
}));

const projectState = vi.hoisted(() => ({
	details: { value: { projectRole: "OWNER" as const, archived: false } },
}));

vi.mock("@/feature/project", () => ({
	useProject: () => ({
		details: projectState.details,
		projectUsers: {
			value: [{ id: 1, username: "admin", projectRole: "OWNER", member: true }],
		},
		...projectActions,
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

enableAutoUnmount(afterEach);

const ticket: Ticket = {
	id: 7,
	projectId: 2,
	ticketTypeId: 3,
	statusId: 4,
	priority: "NORMAL",
	parentTicketId: null,
	name: "Anmeldung reparieren",
	description: "Die Anmeldung schlägt fehl.",
	children: [],
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
	beforeEach(() => {
		vi.clearAllMocks();
		projectState.details.value.archived = false;
	});

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

	it("navigates to the parent ticket", async () => {
		const wrapper = shallowMount(TicketDetail, {
			props: {
				ticket: { ...ticket, parentTicketId: 3 },
				ticketType,
				parentTicketName: "Übergeordnetes Ticket",
				childTickets: [],
			},
			global: {
				stubs: {
					Button: { template: "<button><slot /></button>" },
				},
			},
		});

		expect(wrapper.text()).toContain("Elternticket: Übergeordnetes Ticket");

		await wrapper
			.get('[aria-label="Elternticket Übergeordnetes Ticket öffnen"]')
			.trigger("click");

		expect(wrapper.emitted("selectTicket")).toEqual([[3]]);
	});

	it("archives and deletes only after confirmation", async () => {
		const wrapper = mount(TicketDetail, {
			attachTo: document.body,
			props: { ticket, ticketType, childTickets: [] },
			global: {
				stubs: {
					Tabs: slotStub,
					TabsList: slotStub,
					TabsTrigger: slotStub,
					TabsContent: slotStub,
					Select: slotStub,
					SelectTrigger: slotStub,
					SelectValue: slotStub,
					SelectContent: slotStub,
					SelectItem: slotStub,
					TicketComments: slotStub,
					ActivityTimeline: slotStub,
					CreateTicketForm: slotStub,
				},
			},
		});

		await clickButton(wrapper.element, "Ticket archivieren");
		await nextTick();
		expect(projectActions.archiveTicket).not.toHaveBeenCalled();

		clickDialogButton("Abbrechen");
		await nextTick();
		expect(projectActions.archiveTicket).not.toHaveBeenCalled();

		await clickButton(wrapper.element, "Ticket archivieren");
		await nextTick();
		clickDialogButton("Ticket archivieren");
		await flushPromises();
		expect(projectActions.archiveTicket).toHaveBeenCalledExactlyOnceWith(ticket.id);

		await clickButton(wrapper.element, "Ticket löschen");
		await nextTick();
		expect(projectActions.deleteTicket).not.toHaveBeenCalled();

		clickDialogButton("Ticket löschen");
		await flushPromises();
		expect(projectActions.deleteTicket).toHaveBeenCalledExactlyOnceWith(ticket.id);

		projectActions.deleteTicket.mockRejectedValueOnce(new Error("Request failed"));
		await clickButton(wrapper.element, "Ticket löschen");
		await nextTick();
		clickDialogButton("Ticket löschen");
		await flushPromises();

		expect(projectActions.deleteTicket).toHaveBeenCalledTimes(2);
		expect(getDialog()?.textContent).toContain("Das Ticket konnte nicht gelöscht werden.");
	});

	it("discards an open details draft when the selected ticket changes", async () => {
		const wrapper = mount(TicketDetail, {
			props: { ticket, ticketType, childTickets: [] },
			global: {
				stubs: {
					Tabs: slotStub,
					TabsList: slotStub,
					TabsTrigger: slotStub,
					TabsContent: slotStub,
					Select: slotStub,
					SelectTrigger: slotStub,
					SelectValue: slotStub,
					SelectContent: slotStub,
					SelectItem: slotStub,
					TicketComments: slotStub,
					ActivityTimeline: slotStub,
					CreateTicketForm: slotStub,
				},
			},
		});

		await clickButton(wrapper.element, ticket.name);
		await wrapper.get("input[aria-label='Ticketname']").setValue("Nicht speichern");

		const nextTicket = { ...ticket, id: 8, name: "Anderes Ticket" };
		await wrapper.setProps({ ticket: nextTicket });

		expect(wrapper.find("input[aria-label='Ticketname']").exists()).toBe(false);
		expect(wrapper.text()).toContain(nextTicket.name);
	});

	it("restores an archived ticket without confirmation", async () => {
		let finishRestore!: () => void;
		projectActions.restoreTicket.mockReturnValueOnce(
			new Promise<void>((resolve) => {
				finishRestore = resolve;
			}),
		);
		const wrapper = mount(TicketDetail, {
			attachTo: document.body,
			props: { ticket: { ...ticket, archived: true }, ticketType, childTickets: [] },
			global: {
				stubs: {
					Tabs: slotStub,
					TabsList: slotStub,
					TabsTrigger: slotStub,
					TabsContent: slotStub,
					Select: slotStub,
					SelectTrigger: slotStub,
					SelectValue: slotStub,
					SelectContent: slotStub,
					SelectItem: slotStub,
					TicketComments: slotStub,
					ActivityTimeline: slotStub,
					CreateTicketForm: slotStub,
				},
			},
		});

		await clickButton(wrapper.element, "Ticket wiederherstellen");
		await nextTick();

		expect(projectActions.restoreTicket).toHaveBeenCalledExactlyOnceWith(ticket.id);
		expect(wrapper.emitted("restorePending")).toEqual([[true]]);
		expect(getDialog()).toBeNull();
		expect(getButton(wrapper.element, "Wird wiederhergestellt...").disabled).toBe(true);

		finishRestore();
		await flushPromises();
		expect(wrapper.emitted("restorePending")).toEqual([[true], [false]]);

		projectActions.restoreTicket.mockRejectedValueOnce(new Error("Request failed"));
		await clickButton(wrapper.element, "Ticket wiederherstellen");
		await flushPromises();

		expect(
			wrapper
				.findAll("[role='alert']")
				.some((alert) =>
					alert.text().includes("Das Ticket konnte nicht wiederhergestellt werden."),
				),
		).toBe(true);
	});

	it("disables restoration while the project is archived", () => {
		projectState.details.value.archived = true;
		const wrapper = shallowMount(TicketDetail, {
			props: { ticket: { ...ticket, archived: true }, ticketType, childTickets: [] },
			global: {
				stubs: {
					Button: {
						props: ["disabled"],
						template: '<button :disabled="disabled"><slot /></button>',
					},
				},
			},
		});

		expect(getButton(wrapper.element, "Ticket wiederherstellen").disabled).toBe(true);
	});

	it("disables local ticket mutations for archived tickets", async () => {
		const wrapper = shallowMount(TicketDetail, {
			props: {
				ticket: { ...ticket, archived: true },
				ticketType: { ...ticketType, children: [3] },
				childTickets: [],
			},
			global: {
				stubs: {
					Button: {
						props: ["disabled"],
						template: '<button :disabled="disabled"><slot /></button>',
					},
					Tabs: slotStub,
					TabsList: slotStub,
					TabsTrigger: slotStub,
					TabsContent: slotStub,
					Select: {
						props: ["disabled"],
						template:
							'<div class="select-stub" :data-disabled="String(disabled)"><slot /></div>',
					},
					SelectTrigger: slotStub,
					SelectValue: slotStub,
					SelectContent: slotStub,
					SelectItem: slotStub,
					TicketComments: {
						props: ["disabled"],
						template:
							'<div data-testid="ticket-comments" :data-disabled="String(disabled)" />',
					},
					ActivityTimeline: slotStub,
					CreateTicketForm: {
						props: ["disabled"],
						template:
							'<div data-testid="create-child-ticket" :data-disabled="String(disabled)" />',
					},
				},
			},
		});

		expect(getButton(wrapper.element, ticket.name).disabled).toBe(true);
		expect(getButton(wrapper.element, "Ticket löschen").disabled).toBe(true);
		expect(getButton(wrapper.element, "Ticket wiederherstellen").disabled).toBe(false);
		expect(
			wrapper
				.findAll(".select-stub")
				.every((select) => select.attributes("data-disabled") === "true"),
		).toBe(true);
		expect(wrapper.get("[data-testid='ticket-comments']").attributes("data-disabled")).toBe(
			"true",
		);
		expect(wrapper.get("[data-testid='create-child-ticket']").attributes("data-disabled")).toBe(
			"true",
		);

		getButton(wrapper.element, "Ticket löschen").click();
		await nextTick();
		expect(projectActions.deleteTicket).not.toHaveBeenCalled();
	});
});

const slotStub = { template: "<div><slot /></div>" };

async function clickButton(root: Element, label: string): Promise<void> {
	getButton(root, label).click();
}

function getButton(root: Element, label: string): HTMLButtonElement {
	const button = Array.from(root.querySelectorAll("button")).find(
		(candidate) => candidate.textContent?.trim() === label,
	);
	if (button === undefined) {
		throw new Error(`Expected button ${label}`);
	}

	return button;
}

function clickDialogButton(label: string): void {
	const button = Array.from(getDialog()?.querySelectorAll("button") ?? []).find(
		(candidate) => candidate.textContent?.trim() === label,
	);
	if (button === undefined) {
		throw new Error(`Expected dialog button ${label}`);
	}

	button.click();
}

function getDialog(): Element | null {
	return document.body.querySelector("[data-slot='alert-dialog-content']");
}
