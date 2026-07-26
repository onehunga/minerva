import { createPinia } from "pinia";
import { defineComponent, h, nextTick, ref } from "vue";
import { flushPromises, mount } from "@vue/test-utils";
import { describe, expect, it, vi } from "vitest";
import { TicketRepositoryKey, type ITicketRepository } from "@/feature/ticket/ticket.repository";
import { useProject } from "../composables/useProject";
import { ProjectRepositoryKey, type IProjectRepository } from "../project.repository";

describe("useProject", () => {
	it("loads project data again when the project ID changes", async () => {
		const projectId = ref("1");
		const projectRepository = {
			getProjectDetails: vi.fn<IProjectRepository["getProjectDetails"]>(
				async (id: number) => ({
					id,
					name: `Projekt ${id}`,
					description: "",
					projectRole: "OWNER" as const,
					archived: false,
				}),
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

		const wrapper = mount(host, {
			global: {
				plugins: [createPinia()],
				provide: {
					[ProjectRepositoryKey as symbol]: projectRepository,
					[TicketRepositoryKey as symbol]: ticketRepository,
				},
			},
		});

		await flushPromises();
		expect(projectRepository.getProjectDetails).toHaveBeenLastCalledWith(1);

		projectId.value = "2";
		await nextTick();
		await flushPromises();

		expect(projectRepository.getProjectDetails).toHaveBeenLastCalledWith(2);
		expect(projectRepository.getProjectDetails).toHaveBeenCalledTimes(2);
		wrapper.unmount();
	});
});
