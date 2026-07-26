import { defineComponent, h, nextTick, ref } from "vue";
import { flushPromises, mount } from "@vue/test-utils";
import { describe, expect, it, vi } from "vitest";
import type { ActivityEvent } from "../activity.model";
import { ActivityRepositoryKey, type IActivityRepository } from "../activity.repository";
import { useTicketActivities } from "../composables/useTicketActivities";

describe("useTicketActivities", () => {
	it("keeps the newest activities when an earlier request finishes last", async () => {
		const projectId = ref(1);
		const ticketId = ref(2);
		let resolveFirstActivities!: (events: ActivityEvent[]) => void;
		let resolveSecondActivities!: (events: ActivityEvent[]) => void;
		const firstActivities = new Promise<ActivityEvent[]>((resolve) => {
			resolveFirstActivities = resolve;
		});
		const secondActivities = new Promise<ActivityEvent[]>((resolve) => {
			resolveSecondActivities = resolve;
		});
		const repository = {
			getTicketActivities: vi.fn<IActivityRepository["getTicketActivities"]>(
				async (_projectId, currentTicketId) =>
					currentTicketId === 2 ? firstActivities : secondActivities,
			),
		} satisfies Partial<IActivityRepository>;
		const host = defineComponent({
			setup() {
				const { events } = useTicketActivities(projectId, ticketId);
				return () => h("div", events.value.map((event) => event.id).join(","));
			},
		});

		const wrapper = mount(host, {
			global: {
				provide: {
					[ActivityRepositoryKey as symbol]: repository,
				},
			},
		});

		expect(repository.getTicketActivities).toHaveBeenLastCalledWith(1, 2);

		ticketId.value = 3;
		await nextTick();
		expect(repository.getTicketActivities).toHaveBeenLastCalledWith(1, 3);
		expect(repository.getTicketActivities).toHaveBeenCalledTimes(2);

		resolveSecondActivities([activity(3)]);
		await flushPromises();
		expect(wrapper.text()).toBe("3");

		resolveFirstActivities([activity(2)]);
		await flushPromises();
		expect(wrapper.text()).toBe("3");

		wrapper.unmount();
	});
});

function activity(id: number): ActivityEvent {
	return {
		id,
		type: "TICKET_CREATED",
		schemaVersion: 1,
		actorUserId: null,
		actorUsername: null,
		actorDeleted: false,
		occurredAt: "2026-07-26T10:00:00Z",
		payload: {},
	};
}
