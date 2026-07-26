import { afterEach, describe, expect, it, vi } from "vitest";
import { enableAutoUnmount, flushPromises, mount } from "@vue/test-utils";
import { nextTick } from "vue";
import ProjectPage from "../components/ProjectPage.vue";

const actions = vi.hoisted(() => ({
	archiveProject: vi.fn<(projectId: number) => Promise<void>>(),
	push: vi.fn<(target: unknown) => Promise<void>>(),
}));

vi.mock("vue-router", () => ({
	useRouter: () => ({ push: actions.push }),
}));

vi.mock("@/feature/activity", () => ({
	ActivityTimeline: { template: "<div />" },
	useProjectActivities: () => ({
		events: { value: [] },
		isLoading: { value: false },
		errorMessage: { value: "" },
	}),
}));

vi.mock("@/feature/dashboard", () => ({
	DashboardOverview: { template: "<div />" },
	useProjectDashboard: () => ({
		data: { value: null },
		isLoading: { value: false },
		errorMessage: { value: "" },
	}),
}));

vi.mock("@/feature/ticket", () => ({
	CreateTicketForm: { template: "<div />" },
	TicketDetail: { template: "<div />" },
	TicketList: { template: "<div />" },
}));

vi.mock("@/feature/user", () => ({
	useUserStore: () => ({ userDetails: { role: "USER" } }),
}));

vi.mock("../composables/useProject", async () => {
	const { ref } = await import("vue");

	return {
		useProject: () => ({
			details: ref({
				id: 7,
				name: "Minerva",
				description: "",
				projectRole: "OWNER",
				archived: false,
			}),
			tickets: ref([]),
			ticketTypes: ref([]),
			fetchTickets: vi.fn<() => Promise<void>>(),
			updateProjectDetails: vi.fn<() => Promise<void>>(),
		}),
	};
});

vi.mock("../composables/useProjectRepository", () => ({
	useProjectRepository: () => ({ archiveProject: actions.archiveProject }),
}));

enableAutoUnmount(afterEach);

const slotStub = { template: "<div><slot /></div>" };

describe("ProjectPage", () => {
	it("archives the project only after confirmation", async () => {
		const wrapper = mount(ProjectPage, {
			attachTo: document.body,
			props: { id: 7 },
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
					ProjectUserManagement: slotStub,
					DashboardOverview: slotStub,
					ActivityTimeline: slotStub,
					CreateTicketForm: slotStub,
					TicketDetail: slotStub,
					TicketList: slotStub,
				},
			},
		});

		clickButton(wrapper.element, "Projekt archivieren");
		await nextTick();

		expect(actions.archiveProject).not.toHaveBeenCalled();
		expect(getDialog()?.textContent).toContain("Minerva");

		clickDialogButton("Abbrechen");
		await nextTick();
		expect(actions.archiveProject).not.toHaveBeenCalled();

		clickButton(wrapper.element, "Projekt archivieren");
		await nextTick();
		clickDialogButton("Projekt archivieren");
		await flushPromises();

		expect(actions.archiveProject).toHaveBeenCalledExactlyOnceWith(7);
		expect(actions.push).toHaveBeenCalledWith({ name: "landing" });
	});
});

function getDialog(): Element | null {
	return document.body.querySelector("[data-slot='alert-dialog-content']");
}

function clickButton(root: Element, label: string): void {
	const button = Array.from(root.querySelectorAll("button")).find(
		(candidate) => candidate.textContent?.trim() === label,
	);
	if (button === undefined) {
		throw new Error(`Expected button ${label}`);
	}

	button.click();
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
