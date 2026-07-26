import { beforeEach, describe, expect, it } from "vitest";

import { clearTokens, getAuthorizationHeader, setTokens, type TokenPair } from "@/api";
import { flushPromises, mount } from "@vue/test-utils";
import { createPinia, type Pinia } from "pinia";
import { defineComponent } from "vue";
import type { UserDetails } from "../user.model";
import { UserRepository, UserRepositoryKey } from "../user.repository";
import { useUserStore } from "../user.store";
import { useUser } from "../composables/useUser";

const tokens: TokenPair = {
	accessToken: "access-token",
	refreshToken: "refresh-token",
};

const userDetails: UserDetails = {
	id: 1,
	username: "admin",
	role: "ADMIN",
};

class LoginUserRepository extends UserRepository {
	loginCalls: { username: string; password: string }[] = [];
	logoutCalls: string[] = [];

	override async login(username: string, password: string): Promise<TokenPair> {
		this.loginCalls.push({
			username,
			password,
		});

		return tokens;
	}

	override async details(): Promise<UserDetails> {
		return userDetails;
	}

	override async logout(refreshToken: string): Promise<void> {
		this.logoutCalls.push(refreshToken);
	}
}

class FailingDetailsUserRepository extends LoginUserRepository {
	override async details(): Promise<UserDetails> {
		throw new Error("Request failed");
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

const LogoutHarness = defineComponent({
	setup() {
		const { logout } = useUser();

		return { logout };
	},
	template: `<button type="button" @click="logout">Logout</button>`,
});

describe("useUser", () => {
	beforeEach(() => {
		clearTokens();
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

	it("clears the session when loading user details after login fails", async () => {
		const pinia: Pinia = createPinia();
		const userRepository = new FailingDetailsUserRepository();
		const userStore = useUserStore(pinia);
		userStore.setUserDetails(userDetails);
		const wrapper = mount(LoginHarness, {
			global: {
				plugins: [pinia],
				provide: {
					[UserRepositoryKey]: userRepository,
				},
			},
		});

		await expect(wrapper.vm.login("admin", "secret")).rejects.toThrow("Request failed");

		expect(getAuthorizationHeader()).toBeNull();
		expect(sessionStorage.getItem("minerva.refreshToken")).toBeNull();
		expect(userStore.userDetails).toBeNull();
	});

	it("revokes the refresh token and clears the session", async () => {
		const pinia: Pinia = createPinia();
		const userRepository = new LoginUserRepository();
		const userStore = useUserStore(pinia);
		setTokens(tokens);
		userStore.setUserDetails(userDetails);
		const wrapper = mount(LogoutHarness, {
			global: {
				plugins: [pinia],
				provide: {
					[UserRepositoryKey]: userRepository,
				},
			},
		});

		await wrapper.get("button").trigger("click");
		await flushPromises();

		expect(userRepository.logoutCalls).toEqual(["refresh-token"]);
		expect(getAuthorizationHeader()).toBeNull();
		expect(sessionStorage.getItem("minerva.refreshToken")).toBeNull();
		expect(userStore.userDetails).toBeNull();
	});
});
