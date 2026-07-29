import { createPinia, type Pinia } from "pinia";
import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";
import { enableAutoUnmount, flushPromises, mount } from "@vue/test-utils";
import { nextTick } from "vue";
import type { Ticket } from "@/feature/ticket";
import ProjectPage from "../components/ProjectPage.vue";
import { useProject } from "../composables/useProject";
import { ProjectRepositoryKey, type IProjectRepository } from "../project.repository";
import { useActiveProjectStore, useProjectsStore } from "../project.store";

const routerActions = vi.hoisted(() => ({
	push: vi.fn<(target: unknown) => Promise<void>>(),
	replace: vi.fn<(target: unknown) => Promise<void>>(),
}));
const userState = vi.hoisted(() => ({ role: "USER" }));

vi.mock("vue-router", () => ({
	useRouter: () => ({ push: routerActions.push, replace: routerActions.replace }),
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
	CreateTicketForm: {
		props: ["disabled"],
		template: '<button data-testid="create-ticket" :disabled="disabled" />',
	},
	TicketDetail: {
		emits: ["restorePending"],
		template:
			'<button data-testid="restore-pending" @click="$emit(\'restorePending\', true)" />',
	},
	TicketList: { template: "<div />" },
}));

vi.mock("@/feature/user", () => ({
	useUserStore: () => ({ userDetails: { role: userState.role } }),
}));

vi.mock("../composables/useProject", async () => {
	const { ref } = await import("vue");
	const details = ref({
		id: 7,
		name: "Minerva",
		description: "",
		projectRole: "OWNER" as const,
		archived: false,
	});
	const tickets = ref<Ticket[]>([]);

	return {
		useProject: () => ({
			details,
			tickets,
			ticketTypes: ref([]),
			fetchTickets: async () => undefined,
			updateProjectDetails: async () => undefined,
		}),
	};
});

class FakeProjectRepository implements Pick<
	IProjectRepository,
	"archiveProject" | "restoreProject" | "deleteProject"
> {
	archivedProjectIds: number[] = [];
	restoredProjectIds: number[] = [];
	deletedProjectIds: number[] = [];
	deleteError: Error | null = null;

	async archiveProject(projectId: number): Promise<void> {
		this.archivedProjectIds.push(projectId);
	}

	async restoreProject(projectId: number): Promise<void> {
		this.restoredProjectIds.push(projectId);
	}

	async deleteProject(projectId: number): Promise<void> {
		this.deletedProjectIds.push(projectId);
		if (this.deleteError != null) {
			throw this.deleteError;
		}
	}
}

enableAutoUnmount(afterEach);

const slotStub = { template: "<div><slot /></div>" };

type MountOptions = {
	repository?: FakeProjectRepository;
	pinia?: Pinia;
	projectUserManagement?: object;
};

function mountProjectPage({
	repository = new FakeProjectRepository(),
	pinia = createPinia(),
	projectUserManagement = slotStub,
}: MountOptions = {}) {
	const wrapper = mount(ProjectPage, {
		attachTo: document.body,
		props: { id: 7 },
		global: {
			plugins: [pinia],
			provide: {
				[ProjectRepositoryKey as symbol]: repository,
			},
			stubs: {
				Tabs: slotStub,
				TabsList: slotStub,
				TabsTrigger: {
					props: ["value", "disabled"],
					template: '<button :data-tab="value" :disabled="disabled"><slot /></button>',
				},
				TabsContent: slotStub,
				Select: {
					props: ["disabled"],
					template:
						'<div data-testid="ticket-view-select" :data-disabled="String(disabled)"><slot /></div>',
				},
				SelectTrigger: slotStub,
				SelectValue: slotStub,
				SelectContent: slotStub,
				SelectItem: slotStub,
				ProjectUserManagement: projectUserManagement,
			},
		},
	});

	return { wrapper, repository, pinia };
}

describe("ProjectPage", () => {
	beforeEach(() => {
		vi.clearAllMocks();
		userState.role = "USER";
		const { details, tickets } = useProject();
		if (details.value != null) {
			details.value.archived = false;
			details.value.projectRole = "OWNER";
		}
		tickets.value = [];
	});

	it("archives the project only after confirmation", async () => {
		const { wrapper, repository } = mountProjectPage();

		clickButton(wrapper.element, "Projekt archivieren");
		await nextTick();

		expect(repository.archivedProjectIds).toEqual([]);
		expect(getDialog()?.textContent).toContain("Minerva");

		clickDialogButton("Abbrechen");
		await nextTick();
		expect(repository.archivedProjectIds).toEqual([]);

		clickButton(wrapper.element, "Projekt archivieren");
		await nextTick();
		clickDialogButton("Projekt archivieren");
		await flushPromises();

		expect(repository.archivedProjectIds).toEqual([7]);
		expect(routerActions.push).toHaveBeenCalledWith({ name: "landing" });
	});

	it("disables local project mutations for archived projects", async () => {
		const { details } = useProject();
		if (details.value != null) {
			details.value.archived = true;
		}
		const { wrapper, repository } = mountProjectPage({
			projectUserManagement: {
				props: ["disabled"],
				template: '<div data-testid="project-users" :data-disabled="String(disabled)" />',
			},
		});

		expect(wrapper.get("input").attributes("disabled")).toBeDefined();
		expect(wrapper.get("textarea").attributes("disabled")).toBeDefined();
		expect(wrapper.get("[data-testid='create-ticket']").attributes("disabled")).toBeDefined();
		expect(wrapper.get("[data-testid='project-users']").attributes("data-disabled")).toBe(
			"true",
		);

		expect(repository.archivedProjectIds).toEqual([]);
	});

	it("restores the project and moves it to the member list", async () => {
		const { details } = useProject();
		if (details.value != null) details.value.archived = true;
		const pinia = createPinia();
		const projectsStore = useProjectsStore(pinia);
		projectsStore.setArchivedProjects([{ id: 7, name: "Minerva", description: "" }]);
		const { wrapper, repository } = mountProjectPage({ pinia });

		clickButton(wrapper.element, "Projekt wiederherstellen");
		await flushPromises();

		expect(repository.restoredProjectIds).toEqual([7]);
		expect(projectsStore.archivedProjects).toEqual([]);
		expect(projectsStore.projects).toEqual([{ id: 7, name: "Minerva", description: "" }]);
		expect(details.value?.archived).toBe(false);
	});

	it("restores a non-member project to the admin list", async () => {
		userState.role = "ADMIN";
		const { details } = useProject();
		if (details.value != null) {
			details.value.archived = true;
			details.value.projectRole = null;
		}
		const pinia = createPinia();
		const projectsStore = useProjectsStore(pinia);
		projectsStore.setArchivedAdminProjects([{ id: 7, name: "Minerva", description: "" }]);
		const { wrapper, repository } = mountProjectPage({ pinia });

		clickButton(wrapper.element, "Projekt wiederherstellen");
		await flushPromises();

		expect(repository.restoredProjectIds).toEqual([7]);
		expect(projectsStore.archivedAdminProjects).toEqual([]);
		expect(projectsStore.adminProjects).toEqual([{ id: 7, name: "Minerva", description: "" }]);
	});

	it("opens delete dialog and confirms deletion", async () => {
		const { wrapper, repository } = mountProjectPage();

		expect(repository.deletedProjectIds).toEqual([]);

		clickButton(wrapper.element, "Projekt löschen");
		await nextTick();

		expect(repository.deletedProjectIds).toEqual([]);
		expect(getDialog()?.textContent).toContain("Minerva");

		clickDialogButton("Abbrechen");
		await nextTick();
		expect(repository.deletedProjectIds).toEqual([]);

		clickButton(wrapper.element, "Projekt löschen");
		await nextTick();
		clickDialogButton("Projekt löschen");
		await flushPromises();

		expect(repository.deletedProjectIds).toEqual([7]);
	});

	it("re-enables the ticket view after the restored ticket leaves the list", async () => {
		const { tickets } = useProject();
		tickets.value = [ticket];
		const { wrapper } = mountProjectPage();
		await nextTick();

		await wrapper.get("[data-testid='restore-pending']").trigger("click");
		expect(wrapper.get("[data-testid='ticket-view-select']").attributes("data-disabled")).toBe(
			"true",
		);
		expect(
			wrapper.findAll("[data-tab]").every((tab) => tab.attributes("disabled") != null),
		).toBe(true);

		tickets.value = [];
		await nextTick();

		expect(wrapper.get("[data-testid='ticket-view-select']").attributes("data-disabled")).toBe(
			"false",
		);
		expect(
			wrapper.findAll("[data-tab]").every((tab) => tab.attributes("disabled") == null),
		).toBe(true);
	});

	it("clears store state and navigates on successful deletion", async () => {
		const pinia = createPinia();
		const projectsStore = useProjectsStore(pinia);
		projectsStore.setProjects([
			{ id: 7, name: "Minerva", description: "" },
			{ id: 8, name: "Anderes Projekt", description: "" },
		]);
		projectsStore.setAdminProjects([
			{ id: 7, name: "Minerva", description: "" },
			{ id: 9, name: "Admin-Projekt", description: "" },
		]);

		const activeProjectStore = useActiveProjectStore(pinia);
		activeProjectStore.setActiveProject("7");
		activeProjectStore.setProjectDetails({
			id: 7,
			name: "Minerva",
			description: "",
			projectRole: "OWNER",
			archived: false,
		});
		activeProjectStore.setProjectUsers([{ id: 1, username: "owner", projectRole: "OWNER" }]);
		activeProjectStore.setTicketTypes([
			{
				id: 1,
				name: "Aufgabe",
				description: "",
				states: [],
				transitions: [],
				children: [],
			},
		]);
		activeProjectStore.setTickets([
			{
				id: 1,
				projectId: 7,
				ticketTypeId: 1,
				statusId: 1,
				priority: "NORMAL",
				parentTicketId: null,
				name: "Ticket",
				description: "",
				children: [],
				createdBy: 1,
				assignedTo: null,
				createdAt: null,
				updatedAt: null,
				archived: false,
			},
		]);
		const { wrapper } = mountProjectPage({ pinia });

		clickButton(wrapper.element, "Projekt löschen");
		await nextTick();
		clickDialogButton("Projekt löschen");
		await flushPromises();

		expect(projectsStore.projects).toEqual([
			{ id: 8, name: "Anderes Projekt", description: "" },
		]);
		expect(projectsStore.adminProjects).toEqual([
			{ id: 9, name: "Admin-Projekt", description: "" },
		]);
		expect(activeProjectStore.activeProject).toBeNull();
		expect(activeProjectStore.details).toBeNull();
		expect(activeProjectStore.projectUsers).toEqual([]);
		expect(activeProjectStore.ticketTypes).toEqual([]);
		expect(activeProjectStore.tickets).toEqual([]);
		expect(routerActions.replace).toHaveBeenCalledWith({ name: "projects" });
	});

	it("shows error when deletion fails and keeps dialog open", async () => {
		const repository = new FakeProjectRepository();
		repository.deleteError = new Error("fail");
		const pinia = createPinia();
		const projectsStore = useProjectsStore(pinia);
		projectsStore.setProjects([{ id: 7, name: "Minerva", description: "" }]);
		const { wrapper } = mountProjectPage({ repository, pinia });

		clickButton(wrapper.element, "Projekt löschen");
		await nextTick();
		clickDialogButton("Projekt löschen");
		await flushPromises();

		expect(getDialog()?.textContent).toContain("konnte nicht gelöscht werden");
		expect(repository.deletedProjectIds).toEqual([7]);
		expect(projectsStore.projects).toEqual([{ id: 7, name: "Minerva", description: "" }]);
		expect(routerActions.replace).not.toHaveBeenCalled();
	});
});

const ticket: Ticket = {
	id: 1,
	projectId: 7,
	ticketTypeId: 1,
	statusId: 1,
	priority: "NORMAL",
	parentTicketId: null,
	name: "Archiviertes Ticket",
	description: "",
	children: [],
	createdBy: 1,
	assignedTo: null,
	createdAt: null,
	updatedAt: null,
	archived: true,
};

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
