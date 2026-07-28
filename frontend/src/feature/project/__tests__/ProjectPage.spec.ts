import { createPinia, type Pinia } from "pinia";
import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";
import { enableAutoUnmount, flushPromises, mount } from "@vue/test-utils";
import { nextTick } from "vue";
import ProjectPage from "../components/ProjectPage.vue";
import { useProject } from "../composables/useProject";
import { ProjectRepositoryKey, type IProjectRepository } from "../project.repository";
import { useActiveProjectStore, useProjectsStore } from "../project.store";

const routerActions = vi.hoisted(() => ({
	push: vi.fn<(target: unknown) => Promise<void>>(),
	replace: vi.fn<(target: unknown) => Promise<void>>(),
}));

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
	TicketDetail: { template: "<div />" },
	TicketList: { template: "<div />" },
}));

vi.mock("@/feature/user", () => ({
	useUserStore: () => ({ userDetails: { role: "USER" } }),
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

	return {
		useProject: () => ({
			details,
			tickets: ref([]),
			ticketTypes: ref([]),
			fetchTickets: async () => undefined,
			updateProjectDetails: async () => undefined,
		}),
	};
});

class FakeProjectRepository implements Pick<
	IProjectRepository,
	"archiveProject" | "deleteProject"
> {
	archivedProjectIds: number[] = [];
	deletedProjectIds: number[] = [];
	deleteError: Error | null = null;

	async archiveProject(projectId: number): Promise<void> {
		this.archivedProjectIds.push(projectId);
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
				TabsTrigger: slotStub,
				TabsContent: slotStub,
				Select: slotStub,
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
		const { details } = useProject();
		if (details.value != null) {
			details.value.archived = false;
		}
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

		clickButton(wrapper.element, "Projekt ist archiviert");
		await nextTick();

		expect(repository.archivedProjectIds).toEqual([]);
		expect(getDialog()).toBeNull();
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

	it("clears store state and navigates on successful deletion", async () => {
		const pinia = createPinia();
		const projectsStore = useProjectsStore(pinia);
		projectsStore.setProjects([
			{ id: 7, name: "Minerva" },
			{ id: 8, name: "Anderes Projekt" },
		]);
		projectsStore.setAdminProjects([
			{ id: 7, name: "Minerva" },
			{ id: 9, name: "Admin-Projekt" },
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

		expect(projectsStore.projects).toEqual([{ id: 8, name: "Anderes Projekt" }]);
		expect(projectsStore.adminProjects).toEqual([{ id: 9, name: "Admin-Projekt" }]);
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
		projectsStore.setProjects([{ id: 7, name: "Minerva" }]);
		const { wrapper } = mountProjectPage({ repository, pinia });

		clickButton(wrapper.element, "Projekt löschen");
		await nextTick();
		clickDialogButton("Projekt löschen");
		await flushPromises();

		expect(getDialog()?.textContent).toContain("konnte nicht gelöscht werden");
		expect(repository.deletedProjectIds).toEqual([7]);
		expect(projectsStore.projects).toEqual([{ id: 7, name: "Minerva" }]);
		expect(routerActions.replace).not.toHaveBeenCalled();
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
