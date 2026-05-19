import { describe, expect, it } from "vitest";

import { flushPromises, mount } from "@vue/test-utils";
import { nextTick } from "vue";
import { UserRepository, UserRepositoryKey, type model } from "..";
import UserList from "../components/UserList.vue";

const testUsers: model.UserRecord[] = [
	{
		id: 1,
		username: "admin",
		role: "ADMIN",
	},
	{
		id: 2,
		username: "jane",
		role: "USER",
	},
	{
		id: 3,
		username: "max",
		role: "USER",
	},
];

class MockUserRepository extends UserRepository {
	deletedUserIds: number[] = [];
	updatedUserRoles: { userId: number; role: model.UserRole }[] = [];

	override async getAllUsers(): Promise<model.UserRecordList> {
		return {
			users: testUsers.map((user) => ({ ...user })),
		};
	}

	override async updateUserRole(userId: number, role: model.UserRole): Promise<void> {
		this.updatedUserRoles.push({ userId, role });
	}

	override async deleteUser(userId: number): Promise<void> {
		this.deletedUserIds.push(userId);
	}
}

class EmptyUserRepository extends UserRepository {
	override async getAllUsers(): Promise<model.UserRecordList> {
		return {
			users: [],
		};
	}
}

class FailingUserRepository extends UserRepository {
	override async getAllUsers(): Promise<model.UserRecordList> {
		throw new Error("Failed to load users");
	}
}

class PendingUserRepository extends UserRepository {
	override async getAllUsers(): Promise<model.UserRecordList> {
		return new Promise<model.UserRecordList>(() => {});
	}
}

class FailingDeleteUserRepository extends MockUserRepository {
	override async deleteUser(): Promise<void> {
		throw new Error("Failed to delete user");
	}
}

class FailingUpdateUserRoleRepository extends MockUserRepository {
	override async updateUserRole(): Promise<void> {
		throw new Error("Failed to update user role");
	}
}

describe("UserList", () => {
	it("renders all loaded users", async () => {
		const wrapper = mount(UserList, {
			global: {
				provide: {
					[UserRepositoryKey]: new MockUserRepository(),
				},
			},
		});

		await flushPromises();

		const rows = wrapper.findAll("tbody tr");

		expect(rows).toHaveLength(testUsers.length);

		for (const user of testUsers) {
			expect(wrapper.text()).toContain(user.username);
			expect(wrapper.text()).toContain(user.role);
		}
	});

	it("renders role selects with the current user roles", async () => {
		const wrapper = mount(UserList, {
			global: {
				provide: {
					[UserRepositoryKey]: new MockUserRepository(),
				},
			},
		});

		await flushPromises();

		const selects = wrapper.findAll("select");

		expect(selects).toHaveLength(testUsers.length);
		expect((selects[0]?.element as HTMLSelectElement | undefined)?.value).toBe("ADMIN");
		expect((selects[1]?.element as HTMLSelectElement | undefined)?.value).toBe("USER");
		expect((selects[2]?.element as HTMLSelectElement | undefined)?.value).toBe("USER");
	});

	it("updates a user role and reflects it in the list", async () => {
		const userRepository = new MockUserRepository();
		const wrapper = mount(UserList, {
			global: {
				provide: {
					[UserRepositoryKey]: userRepository,
				},
			},
		});

		await flushPromises();

		const janeRoleSelect = wrapper.findAll("select")[1];
		if (janeRoleSelect === undefined) {
			throw new Error("Expected jane role select to exist");
		}
		await janeRoleSelect.setValue("ADMIN");
		await flushPromises();

		expect(userRepository.updatedUserRoles).toEqual([{ userId: 2, role: "ADMIN" }]);
		expect((janeRoleSelect.element as HTMLSelectElement).value).toBe("ADMIN");
	});

	it("renders an alert and restores the previous role when updating a role fails", async () => {
		const wrapper = mount(UserList, {
			global: {
				provide: {
					[UserRepositoryKey]: new FailingUpdateUserRoleRepository(),
				},
			},
		});

		await flushPromises();

		const janeRoleSelect = wrapper.findAll("select")[1];
		if (janeRoleSelect === undefined) {
			throw new Error("Expected jane role select to exist");
		}
		await janeRoleSelect.setValue("ADMIN");
		await flushPromises();

		const alert = wrapper.get("[role='alert']");

		expect(alert.text()).toBe('Rolle von Benutzer "jane" konnte nicht aktualisiert werden.');
		expect((janeRoleSelect.element as HTMLSelectElement).value).toBe("USER");
	});

	it("deletes a user and removes it from the list", async () => {
		const userRepository = new MockUserRepository();
		const wrapper = mount(UserList, {
			global: {
				provide: {
					[UserRepositoryKey]: userRepository,
				},
			},
		});

		await flushPromises();

		const janeRow = wrapper.findAll("tbody tr")[1];
		if (janeRow === undefined) {
			throw new Error("Expected jane row to exist");
		}
		await janeRow.get("button").trigger("click");
		await flushPromises();

		expect(userRepository.deletedUserIds).toEqual([2]);
		expect(wrapper.text()).not.toContain("jane");
		expect(wrapper.findAll("tbody tr")).toHaveLength(2);
	});

	it("renders an alert when deleting a user fails", async () => {
		const wrapper = mount(UserList, {
			global: {
				provide: {
					[UserRepositoryKey]: new FailingDeleteUserRepository(),
				},
			},
		});

		await flushPromises();

		const janeRow = wrapper.findAll("tbody tr")[1];
		if (janeRow === undefined) {
			throw new Error("Expected jane row to exist");
		}
		await janeRow.get("button").trigger("click");
		await flushPromises();

		const alert = wrapper.get("[role='alert']");

		expect(alert.text()).toBe('Benutzer "jane" konnte nicht gelöscht werden.');
		expect(wrapper.text()).toContain("jane");
	});

	it("renders the empty state when no users are loaded", async () => {
		const wrapper = mount(UserList, {
			global: {
				provide: {
					[UserRepositoryKey]: new EmptyUserRepository(),
				},
			},
		});

		await flushPromises();

		expect(wrapper.text()).toContain("Es sind noch keine Benutzer vorhanden.");
		expect(wrapper.find("table").exists()).toBe(false);
	});

	it("renders an alert when loading users fails", async () => {
		const wrapper = mount(UserList, {
			global: {
				provide: {
					[UserRepositoryKey]: new FailingUserRepository(),
				},
			},
		});

		await flushPromises();

		const alert = wrapper.get("[role='alert']");

		expect(alert.text()).toBe("Benutzer konnten nicht geladen werden.");
		expect(wrapper.find("table").exists()).toBe(false);
	});

	it("renders a loading state while users are loading", async () => {
		const wrapper = mount(UserList, {
			global: {
				provide: {
					[UserRepositoryKey]: new PendingUserRepository(),
				},
			},
		});

		await nextTick();

		expect(wrapper.text()).toContain("Benutzer werden geladen...");
		expect(wrapper.find("table").exists()).toBe(false);
	});
});
