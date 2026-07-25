// oxlint-disable no-unused-vars
import { describe, expect, it } from "vitest";

import { flushPromises, mount } from "@vue/test-utils";
import { Select } from "@/components/ui/select";
import { UserRepositoryKey, type IUserRepository, type model } from "..";
import UserCreateForm from "../components/UserCreateForm.vue";
import type { TokenPair } from "@/api/credentials.ts";

type CreateUserCall = {
	username: string;
	password: string;
	role: model.UserRole;
};

class RecordingUserRepository implements IUserRepository {
	login(username: string, password: string): Promise<TokenPair> {
		throw new Error("Method not implemented.");
	}
	logout(refreshToken: string): Promise<void> {
		throw new Error("Method not implemented.");
	}
	details(): Promise<model.UserDetails> {
		throw new Error("Method not implemented.");
	}
	getAllUsers(): Promise<model.UserRecordList> {
		throw new Error("Method not implemented.");
	}
	updateUserRole(userId: number, role: model.UserRole): Promise<void> {
		throw new Error("Method not implemented.");
	}
	updateUsername(userId: number, username: string): Promise<void> {
		throw new Error("Method not implemented.");
	}
	updateUserPassword(userId: number, password: string): Promise<void> {
		throw new Error("Method not implemented.");
	}
	deleteUser(userId: number): Promise<void> {
		throw new Error("Method not implemented.");
	}
	calls: CreateUserCall[] = [];

	async createUser(username: string, password: string, role: model.UserRole): Promise<void> {
		this.calls.push({
			username,
			password,
			role,
		});
	}
}

class FailingCreateUserRepository implements IUserRepository {
	login(username: string, password: string): Promise<TokenPair> {
		throw new Error("Method not implemented.");
	}
	logout(refreshToken: string): Promise<void> {
		throw new Error("Method not implemented.");
	}
	details(): Promise<model.UserDetails> {
		throw new Error("Method not implemented.");
	}
	getAllUsers(): Promise<model.UserRecordList> {
		throw new Error("Method not implemented.");
	}
	updateUserRole(userId: number, role: model.UserRole): Promise<void> {
		throw new Error("Method not implemented.");
	}
	updateUsername(userId: number, username: string): Promise<void> {
		throw new Error("Method not implemented.");
	}
	updateUserPassword(userId: number, password: string): Promise<void> {
		throw new Error("Method not implemented.");
	}
	deleteUser(userId: number): Promise<void> {
		throw new Error("Method not implemented.");
	}
	async createUser(): Promise<void> {
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
		wrapper.findComponent(Select).vm.$emit("update:modelValue", "ADMIN");
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
		await wrapper.get("form").trigger("submit");
		await flushPromises();

		const alert = wrapper.get("[role='alert']");

		expect(alert.text()).toBe("Benutzer konnte nicht erstellt werden.");
		expect(wrapper.emitted("created")).toBeUndefined();
	});
});
