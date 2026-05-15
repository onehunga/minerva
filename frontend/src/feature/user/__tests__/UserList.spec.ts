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
	override async getAllUsers(): Promise<model.UserRecordList> {
		return {
			users: testUsers,
		};
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
