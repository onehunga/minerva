import { describe, it, expect } from "vitest";

import { mount } from "@vue/test-utils";
import LoginView from "@/views/LoginView.vue";

describe("ShowsLogin", () => {
	it("renders", () => {
		const wrapper = mount(LoginView);
		expect(wrapper.exists()).toBe(true);
	});
});
