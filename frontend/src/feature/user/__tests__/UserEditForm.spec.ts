// oxlint-disable no-unused-vars
import { describe, expect, it } from "vitest";
import { flushPromises, mount } from "@vue/test-utils";
import { Select } from "@/components/ui/select";
import type { UserDetails, UserRecord, UserRecordList, UserRole } from "../user.model";
import { type UserRepository, UserRepositoryKey, type IUserRepository } from "../user.repository";
import UserEditForm from "../components/UserEditForm.vue";
import type { TokenPair } from "@/api/credentials.ts";

class RecordingUserRepository implements IUserRepository {
	updatedRoles: { userId: number; role: UserRole }[] = [];
	updatedUsernames: { userId: number; username: string }[] = [];
	updatedPasswords: { userId: number; password: string }[] = [];

	async updateUserRole(userId: number, role: UserRole): Promise<void> {
		this.updatedRoles.push({ userId, role });
	}

	async updateUsername(userId: number, username: string): Promise<void> {
		this.updatedUsernames.push({ userId, username });
	}

	async updateUserPassword(userId: number, password: string): Promise<void> {
		this.updatedPasswords.push({ userId, password });
	}

	login(username: string, password: string): Promise<TokenPair> {
		throw new Error("Method not implemented.");
	}
	logout(refreshToken: string): Promise<void> {
		throw new Error("Method not implemented.");
	}
	details(): Promise<UserDetails> {
		throw new Error("Method not implemented.");
	}
	createUser(username: string, password: string, role: UserRole): Promise<void> {
		throw new Error("Method not implemented.");
	}
	getAllUsers(): Promise<UserRecordList> {
		throw new Error("Method not implemented.");
	}
	deleteUser(userId: number): Promise<void> {
		throw new Error("Method not implemented.");
	}
}

const user: UserRecord = {
	id: 2,
	username: "jane",
	role: "USER",
};

describe("UserEditForm", () => {
	it("updates all changed details in one form", async () => {
		const userRepository = new RecordingUserRepository();
		const editableUser = { ...user };
		const wrapper = mountForm(userRepository, editableUser);

		await wrapper.get("#edit-username-2").setValue("jane-new");
		await wrapper.get("#edit-password-2").setValue("new-password");
		await wrapper.get("#edit-password-confirmation-2").setValue("new-password");
		wrapper.findComponent(Select).vm.$emit("update:modelValue", "ADMIN");
		await wrapper.get("form").trigger("submit");
		await flushPromises();

		expect(userRepository.updatedUsernames).toEqual([{ userId: 2, username: "jane-new" }]);
		expect(userRepository.updatedPasswords).toEqual([{ userId: 2, password: "new-password" }]);
		expect(userRepository.updatedRoles).toEqual([{ userId: 2, role: "ADMIN" }]);
		expect(editableUser.username).toBe("jane-new");
		expect(editableUser.role).toBe("ADMIN");
		expect(wrapper.emitted("saved")).toHaveLength(1);
	});

	it("leaves an empty password unchanged", async () => {
		const userRepository = new RecordingUserRepository();
		const wrapper = mountForm(userRepository, { ...user });

		await wrapper.get("#edit-username-2").setValue("jane-new");
		await wrapper.get("form").trigger("submit");
		await flushPromises();

		expect(userRepository.updatedUsernames).toEqual([{ userId: 2, username: "jane-new" }]);
		expect(userRepository.updatedPasswords).toEqual([]);
		expect(userRepository.updatedRoles).toEqual([]);
	});

	it("can disable role editing", () => {
		const userRepository = new RecordingUserRepository();
		const wrapper = mountForm(userRepository, { ...user }, false);

		expect(wrapper.findComponent(Select).props("disabled")).toBe(true);
		expect(wrapper.get("#edit-username-2").attributes("disabled")).toBeUndefined();
		expect(wrapper.get("#edit-password-2").attributes("disabled")).toBeUndefined();
		expect(wrapper.get("#edit-password-confirmation-2").attributes("disabled")).toBeUndefined();
	});

	it("rejects different passwords without updating the user", async () => {
		const userRepository = new RecordingUserRepository();
		const wrapper = mountForm(userRepository, { ...user });

		await wrapper.get("#edit-password-2").setValue("new-password");
		await wrapper.get("#edit-password-confirmation-2").setValue("other-password");
		await wrapper.get("form").trigger("submit");

		expect(userRepository.updatedPasswords).toEqual([]);
		expect(wrapper.get("[role='alert']").text()).toBe("Die Passwörter stimmen nicht überein.");
		expect(wrapper.emitted("saved")).toBeUndefined();
	});
});

function mountForm(
	userRepository: UserRepository,
	editableUser: UserRecord,
	canEditRole: boolean = true,
) {
	return mount(UserEditForm, {
		props: {
			user: editableUser,
			canEditRole,
		},
		global: {
			provide: {
				[UserRepositoryKey]: userRepository,
			},
		},
	});
}
