import { describe, it, expect } from "vitest";

import { mount } from "@vue/test-utils";
import { createPinia, type Pinia } from "pinia";
import LoginView from "@/views/LoginView.vue";
import router from "@/router";
import { UserRepository, UserRepositoryKey } from "..";

describe("ShowsLogin", () => {
	it("renders", async () => {
		const pinia: Pinia = createPinia();
		const wrapper = mount(LoginView, {
			global: {
				plugins: [pinia, router],
				provide: {
					[UserRepositoryKey]: new UserRepository(),
				},
			},
		});

		await router.isReady();

		expect(wrapper.exists()).toBe(true);
	});
});
