import { describe, it, expect } from "vitest";

import { flushPromises, mount } from "@vue/test-utils";
import { createPinia, type Pinia } from "pinia";
import LoginView from "@/views/LoginView.vue";
import router from "@/router";
import { UserRepository, UserRepositoryKey } from "../user.repository";

class FailingLoginRepository extends UserRepository {
	override async login(): Promise<never> {
		throw new Error("Login failed");
	}
}

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

	it("renders a non-blocking error when login fails", async () => {
		const wrapper = mount(LoginView, {
			global: {
				plugins: [createPinia(), router],
				provide: {
					[UserRepositoryKey]: new FailingLoginRepository(),
				},
			},
		});

		await wrapper.get("#username").setValue("julian");
		await wrapper.get("#password").setValue("wrong-password");
		await wrapper.get("form").trigger("submit");
		await flushPromises();

		expect(wrapper.get("[role='alert']").text()).toBe("Login failed");
	});
});
