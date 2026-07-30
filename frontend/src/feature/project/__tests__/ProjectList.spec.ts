import { flushPromises, mount } from "@vue/test-utils";
import { describe, expect, it } from "vitest";
import { createMemoryHistory, createRouter } from "vue-router";
import ProjectList from "../components/ProjectList.vue";

describe("ProjectList", () => {
	it("renders the passed projects and opens the selected project", async () => {
		const router = createRouter({
			history: createMemoryHistory(),
			routes: [
				{ path: "/", component: { template: "<div />" } },
				{
					path: "/project/:id",
					name: "project",
					component: { template: "<div />" },
				},
			],
		});
		await router.push("/");
		await router.isReady();

		const wrapper = mount(ProjectList, {
			props: {
				projects: [
					{ id: 1, name: "Erstes Projekt", description: "Beschreibung" },
					{ id: 2, name: "Zweites Projekt", description: "" },
				],
			},
			global: {
				plugins: [router],
			},
		});

		expect(wrapper.text()).toContain("Erstes Projekt");
		expect(wrapper.text()).toContain("Zweites Projekt");
		expect(wrapper.text()).toContain("Keine Beschreibung hinterlegt.");

		await wrapper.findAll("tbody tr")[0]?.trigger("click");
		await flushPromises();

		expect(router.currentRoute.value.fullPath).toBe("/project/1");
	});

	it("shows the contextual empty state and optional create action", async () => {
		const router = createRouter({
			history: createMemoryHistory(),
			routes: [
				{ path: "/", component: { template: "<div />" } },
				{ path: "/create-project", component: { template: "<div />" } },
			],
		});
		await router.push("/");
		await router.isReady();

		const wrapper = mount(ProjectList, {
			props: {
				projects: [],
				emptyTitle: "Noch keine Projekte",
				emptyDescription: "Erstelle dein erstes Projekt.",
				showCreateAction: true,
			},
			global: { plugins: [router] },
		});

		expect(wrapper.text()).toContain("Noch keine Projekte");
		await wrapper.get("button").trigger("click");
		await flushPromises();
		expect(router.currentRoute.value.fullPath).toBe("/create-project");
	});
});
