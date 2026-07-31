import { mount } from "@vue/test-utils";
import { describe, expect, it, vi } from "vitest";
import ActivityTimeline from "../components/ActivityTimeline.vue";

vi.mock("@/feature/user", () => ({
	useUsers: () => ({ users: { value: [{ id: 1, username: "admin" }] } }),
}));

describe("ActivityTimeline", () => {
	it("shows a structural skeleton while loading", () => {
		const wrapper = mount(ActivityTimeline, {
			props: { events: [], isLoading: true, errorMessage: "" },
		});

		expect(wrapper.get("section").attributes("aria-busy")).toBe("true");
		expect(
			wrapper.get("[data-testid='activity-skeleton']").findAll("[data-slot='skeleton']"),
		).toHaveLength(9);
		expect(wrapper.text()).toContain("Aktivitäten werden geladen...");
	});

	it("describes a restored ticket", () => {
		const wrapper = mount(ActivityTimeline, {
			props: {
				events: [
					{
						id: 1,
						type: "TICKET_RESTORED",
						schemaVersion: 1,
						actorUserId: 1,
						occurredAt: "2026-07-28T12:00:00Z",
						payload: { name: "Anmeldung reparieren" },
					},
				],
				isLoading: false,
				errorMessage: "",
			},
			global: {
				stubs: {
					ScrollArea: { template: "<div><slot /></div>" },
				},
			},
		});

		expect(wrapper.text()).toContain("Ticket wiederhergestellt");
		expect(wrapper.text()).toContain("Anmeldung reparieren");
		expect(wrapper.find("[data-activity-category='ticket']").exists()).toBe(true);
	});

	it("uses one icon category for project, ticket, and user events", () => {
		const wrapper = mount(ActivityTimeline, {
			props: {
				events: [
					activity(1, "PROJECT_CREATED"),
					activity(2, "TICKET_CREATED"),
					activity(3, "USER_CREATED"),
				],
				isLoading: false,
				errorMessage: "",
			},
			global: { stubs: { ScrollArea: { template: "<div><slot /></div>" } } },
		});

		expect(wrapper.findAll("[data-activity-category='project']")).toHaveLength(1);
		expect(wrapper.findAll("[data-activity-category='ticket']")).toHaveLength(1);
		expect(wrapper.findAll("[data-activity-category='user']")).toHaveLength(1);
	});
});

function activity(id: number, type: "PROJECT_CREATED" | "TICKET_CREATED" | "USER_CREATED") {
	return {
		id,
		type,
		schemaVersion: 1,
		actorUserId: 1,
		occurredAt: "2026-07-28T12:00:00Z",
		payload: { name: "Eintrag", username: "admin", role: "USER" },
	};
}
