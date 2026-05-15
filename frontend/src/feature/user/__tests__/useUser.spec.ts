import { beforeEach, describe, expect, it } from "vitest";

import { getAuthorizationHeader, type TokenPair } from "@/api";
import { flushPromises, mount } from "@vue/test-utils";
import { createPinia, type Pinia } from "pinia";
import { defineComponent } from "vue";
import { UserRepository, UserRepositoryKey, useUser, useUserStore, type model } from "..";

const tokens: TokenPair = {
	accessToken: "access-token",
	refreshToken: "refresh-token",
};

const userDetails: model.UserDetails = {
	id: 1,
	username: "admin",
	role: "ADMIN",
};

class LoginUserRepository extends UserRepository {
	loginCalls: { username: string; password: string }[] = [];

	override async login(username: string, password: string): Promise<TokenPair> {
		this.loginCalls.push({
			username,
			password,
		});

		return tokens;
	}

	override async details(): Promise<model.UserDetails> {
		return userDetails;
	}
}

const LoginHarness = defineComponent({
	setup() {
		const { login } = useUser();

		return {
			login,
		};
	},
	template: `<button type="button" @click="login('admin', 'secret')">Login</button>`,
});

describe("useUser", () => {
	beforeEach(() => {
		sessionStorage.clear();
	});

	it("logs in, stores tokens, and loads user details", async () => {
		const pinia: Pinia = createPinia();
		const userRepository = new LoginUserRepository();
		const wrapper = mount(LoginHarness, {
			global: {
				plugins: [pinia],
				provide: {
					[UserRepositoryKey]: userRepository,
				},
			},
		});

		await wrapper.get("button").trigger("click");
		await flushPromises();

		const userStore = useUserStore(pinia);

		expect(userRepository.loginCalls).toEqual([
			{
				username: "admin",
				password: "secret",
			},
		]);
		expect(getAuthorizationHeader()).toBe("Bearer access-token");
		expect(sessionStorage.getItem("minerva.refreshToken")).toBe("refresh-token");
		expect(userStore.userDetails).toEqual(userDetails);
	});
});
