import { describe, expect, it } from "vitest";

import { flushPromises, mount } from "@vue/test-utils";
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
});
