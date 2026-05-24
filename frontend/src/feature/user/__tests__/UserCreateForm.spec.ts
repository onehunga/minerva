import { describe, expect, it } from "vitest";

import { flushPromises, mount } from "@vue/test-utils";
import { UserRepository, UserRepositoryKey, type model } from "..";
import UserCreateForm from "../components/UserCreateForm.vue";

type CreateUserCall = {
	username: string;
	password: string;
	role: model.UserRole;
};

class RecordingUserRepository extends UserRepository {
	calls: CreateUserCall[] = [];

	override async createUser(
		username: string,
		password: string,
		role: model.UserRole,
	): Promise<void> {
		this.calls.push({
			username,
			password,
			role,
		});
	}
}

class FailingCreateUserRepository extends UserRepository {
	override async createUser(): Promise<void> {
		throw new Error("Failed to create user");
	}
}

describe("UserCreateForm", () => {
	it("creates a user and emits created", async () => {
		const userRepository = new RecordingUserRepository();
		const wrapper = mount(UserCreateForm, {
			global: {
				provide: {
					[UserRepositoryKey]: userRepository,
				},
			},
		});

		await wrapper.get("#username").setValue("new-user");
		await wrapper.get("#password").setValue("secret-password");
		await wrapper.get("#role").setValue("ADMIN");
		await wrapper.get("form").trigger("submit");
		await flushPromises();

		expect(userRepository.calls).toEqual([
			{
				username: "new-user",
				password: "secret-password",
				role: "ADMIN",
			},
		]);
		expect(wrapper.emitted("created")).toHaveLength(1);
		expect(wrapper.text()).toContain("Benutzer wurde erstellt.");
	});

	it("renders an alert and does not emit created when creation fails", async () => {
		const wrapper = mount(UserCreateForm, {
			global: {
				provide: {
					[UserRepositoryKey]: new FailingCreateUserRepository(),
				},
			},
		});

		await wrapper.get("#username").setValue("new-user");
		await wrapper.get("#password").setValue("secret-password");
		await wrapper.get("#role").setValue("USER");
		await wrapper.get("form").trigger("submit");
		await flushPromises();

		const alert = wrapper.get("[role='alert']");

		expect(alert.text()).toBe("Benutzer konnte nicht erstellt werden.");
		expect(wrapper.emitted("created")).toBeUndefined();
	});
});
