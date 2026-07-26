import { defineComponent, h, nextTick, ref } from "vue";
import { flushPromises, mount } from "@vue/test-utils";
import { describe, expect, it, vi } from "vitest";
import { ActivityRepositoryKey, type IActivityRepository } from "../activity.repository";
import { useTicketActivities } from "../composables/useTicketActivities";

describe("useTicketActivities", () => {
	it("loads activities again when the ticket ID changes", async () => {
		const projectId = ref(1);
		const ticketId = ref(2);
		const repository = {
			getTicketActivities: vi.fn<IActivityRepository["getTicketActivities"]>(async () => []),
		} satisfies Partial<IActivityRepository>;
		const host = defineComponent({
			setup() {
				useTicketActivities(projectId, ticketId);
				return () => h("div");
			},
		});

		const wrapper = mount(host, {
			global: {
				provide: {
					[ActivityRepositoryKey as symbol]: repository,
				},
			},
		});

		await flushPromises();
		expect(repository.getTicketActivities).toHaveBeenLastCalledWith(1, 2);

		ticketId.value = 3;
		await nextTick();
		await flushPromises();

		expect(repository.getTicketActivities).toHaveBeenLastCalledWith(1, 3);
		expect(repository.getTicketActivities).toHaveBeenCalledTimes(2);
		wrapper.unmount();
	});
});
