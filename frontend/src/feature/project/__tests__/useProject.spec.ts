import { createPinia } from "pinia";
import { defineComponent, h, nextTick, ref } from "vue";
import { flushPromises, mount } from "@vue/test-utils";
import { describe, expect, it, vi } from "vitest";
import { TicketRepositoryKey, type ITicketRepository } from "@/feature/ticket/ticket.repository";
import { useProject } from "../composables/useProject";
import type { ProjectDetails } from "../project.model";
import { ProjectRepositoryKey, type IProjectRepository } from "../project.repository";
import { useActiveProjectStore } from "../project.store";

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
});
