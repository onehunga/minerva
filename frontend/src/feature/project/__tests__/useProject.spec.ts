import { createPinia } from "pinia";
import { defineComponent, h, nextTick, ref } from "vue";
import { flushPromises, mount } from "@vue/test-utils";
import { describe, expect, it, vi } from "vitest";
import { TicketRepositoryKey, type ITicketRepository } from "@/feature/ticket/ticket.repository";
import type { Ticket } from "@/feature/ticket/ticket.model";
import { useProject } from "../composables/useProject";
import type { ProjectDetails } from "../project.model";
import { ProjectRepositoryKey, type IProjectRepository } from "../project.repository";
import { useActiveProjectStore } from "../project.store";

class FakeTicketRepository implements Pick<ITicketRepository, "restoreTicket"> {
	restoredTickets: Array<{ projectId: number; ticketId: number }> = [];
	restoreResult: Promise<void> = Promise.resolve();

	restoreTicket(projectId: number, ticketId: number): Promise<void> {
		this.restoredTickets.push({ projectId, ticketId });
		return this.restoreResult;
	}
}

const childTicket: Ticket = {
	id: 2,
	projectId: 7,
	ticketTypeId: 1,
	statusId: 1,
	priority: "NORMAL",
	parentTicketId: 1,
	name: "Child",
	description: "",
	children: [],
	createdBy: 1,
	assignedTo: null,
	createdAt: null,
	updatedAt: null,
	archived: true,
};

const parentTicket: Ticket = {
	...childTicket,
	id: 1,
	parentTicketId: null,
	name: "Parent",
	children: [{ id: 2, name: "Child", description: "" }],
};

describe("useProject", () => {
	it("keeps the newest project data when an earlier request finishes last", async () => {
		const projectId = ref("1");
		let resolveFirstProject!: (project: ProjectDetails) => void;
		const firstProject = new Promise<ProjectDetails>((resolve) => {
			resolveFirstProject = resolve;
		});
		const projectRepository = {
			getProjectDetails: vi.fn<IProjectRepository["getProjectDetails"]>(async (id: number) =>
				id === 1
					? firstProject
					: {
							id,
							name: `Projekt ${id}`,
							description: "",
							projectRole: "OWNER" as const,
							archived: false,
						},
			),
			getProjectUsers: vi.fn<IProjectRepository["getProjectUsers"]>(async () => []),
		} satisfies Partial<IProjectRepository>;
		const ticketRepository = {
			getTicketTypes: vi.fn<ITicketRepository["getTicketTypes"]>(async () => []),
			getTickets: vi.fn<ITicketRepository["getTickets"]>(async () => []),
		} satisfies Partial<ITicketRepository>;
		const host = defineComponent({
			setup() {
				useProject(projectId);
				return () => h("div");
			},
		});
		const pinia = createPinia();

		const wrapper = mount(host, {
			global: {
				plugins: [pinia],
				provide: {
					[ProjectRepositoryKey as symbol]: projectRepository,
					[TicketRepositoryKey as symbol]: ticketRepository,
				},
			},
		});

		expect(projectRepository.getProjectDetails).toHaveBeenCalledExactlyOnceWith(1);

		projectId.value = "2";
		await nextTick();
		await flushPromises();

		expect(projectRepository.getProjectDetails).toHaveBeenLastCalledWith(2);
		expect(projectRepository.getProjectDetails).toHaveBeenCalledTimes(2);
		expect(useActiveProjectStore(pinia).details?.id).toBe(2);

		resolveFirstProject({
			id: 1,
			name: "Projekt 1",
			description: "",
			projectRole: "OWNER",
			archived: false,
		});
		await flushPromises();

		expect(useActiveProjectStore(pinia).details?.id).toBe(2);
		wrapper.unmount();
	});

	it("removes a restored ticket and its child reference only after success", async () => {
		const pinia = createPinia();
		const store = useActiveProjectStore(pinia);
		store.setActiveProject("7");
		store.setTickets(
			[{ ...parentTicket, children: [...parentTicket.children] }, childTicket],
			true,
		);
		const ticketRepository = new FakeTicketRepository();
		let finishRestore!: () => void;
		ticketRepository.restoreResult = new Promise<void>((resolve) => {
			finishRestore = resolve;
		});
		let restoreTicket!: (ticketId: number) => Promise<void>;
		const host = defineComponent({
			setup() {
				restoreTicket = useProject().restoreTicket;
				return () => h("div");
			},
		});

		const wrapper = mount(host, {
			global: {
				plugins: [pinia],
				provide: {
					[ProjectRepositoryKey as symbol]: {},
					[TicketRepositoryKey as symbol]: ticketRepository,
				},
			},
		});

		const request = restoreTicket(childTicket.id);
		expect(ticketRepository.restoredTickets).toEqual([{ projectId: 7, ticketId: 2 }]);
		expect(store.tickets).toHaveLength(2);
		expect(store.tickets[0]?.children).toHaveLength(1);

		finishRestore();
		await request;

		expect(store.tickets).toEqual([{ ...parentTicket, children: [] }]);
		wrapper.unmount();
	});

	it("keeps an archived ticket when restoration fails", async () => {
		const pinia = createPinia();
		const store = useActiveProjectStore(pinia);
		store.setActiveProject("7");
		store.setTickets([childTicket], true);
		const ticketRepository = new FakeTicketRepository();
		ticketRepository.restoreResult = Promise.reject(new Error("Request failed"));
		let restoreTicket!: (ticketId: number) => Promise<void>;
		const host = defineComponent({
			setup() {
				restoreTicket = useProject().restoreTicket;
				return () => h("div");
			},
		});

		const wrapper = mount(host, {
			global: {
				plugins: [pinia],
				provide: {
					[ProjectRepositoryKey as symbol]: {},
					[TicketRepositoryKey as symbol]: ticketRepository,
				},
			},
		});

		await expect(restoreTicket(childTicket.id)).rejects.toThrow("Request failed");
		expect(store.tickets).toEqual([childTicket]);
		wrapper.unmount();
	});
});
