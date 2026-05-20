import { afterEach, describe, expect, it } from "vitest";

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
	updatedUsernames: { userId: number; username: string }[] = [];
	updatedUserPasswords: { userId: number; password: string }[] = [];

	override async getAllUsers(): Promise<model.UserRecordList> {
		return {
			users: testUsers.map((user) => ({ ...user })),
		};
	}

	override async updateUserRole(userId: number, role: model.UserRole): Promise<void> {
		this.updatedUserRoles.push({ userId, role });
	}

	override async updateUsername(userId: number, username: string): Promise<void> {
		this.updatedUsernames.push({ userId, username });
	}

	override async updateUserPassword(userId: number, password: string): Promise<void> {
		this.updatedUserPasswords.push({ userId, password });
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

class FailingUpdateUsernameRepository extends MockUserRepository {
	override async updateUsername(): Promise<void> {
		throw new Error("Failed to update username");
	}
}

describe("UserList", () => {
	afterEach(() => {
		document.body.innerHTML = "";
	});

	it("renders all loaded users", async () => {
		const wrapper = mount(UserList, {
			attachTo: document.body,
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

	it("renders edit action buttons instead of role selects", async () => {
		const wrapper = mount(UserList, {
			attachTo: document.body,
			global: {
				provide: {
					[UserRepositoryKey]: new MockUserRepository(),
				},
			},
		});

		await flushPromises();

		expect(wrapper.findAll("select")).toHaveLength(0);
		expect(wrapper.findAll("[aria-label$='bearbeiten']")).toHaveLength(testUsers.length);
	});

	it("opens the edit modal with the current user data", async () => {
		const wrapper = mount(UserList, {
			attachTo: document.body,
			global: {
				provide: {
					[UserRepositoryKey]: new MockUserRepository(),
				},
			},
		});

		await flushPromises();

		await wrapper.get('[aria-label="Benutzer jane bearbeiten"]').trigger("click");
		await nextTick();

		const dialog = document.body.querySelector("[role='dialog']");
		const usernameInput = document.body.querySelector<HTMLInputElement>("#edit-username-2");
		const roleSelect = document.body.querySelector<HTMLSelectElement>("#edit-role-2");

		expect(dialog?.textContent).toContain("Benutzer bearbeiten");
		expect(usernameInput?.value).toBe("jane");
		expect(roleSelect?.value).toBe("USER");
	});

	it("updates changed username and role without changing an empty password", async () => {
		const userRepository = new MockUserRepository();
		const wrapper = mount(UserList, {
			attachTo: document.body,
			global: {
				provide: {
					[UserRepositoryKey]: userRepository,
				},
			},
		});

		await flushPromises();

		await wrapper.get('[aria-label="Benutzer jane bearbeiten"]').trigger("click");
		await nextTick();
		setInputValue("#edit-username-2", "jane-new");
		setSelectValue("#edit-role-2", "ADMIN");
		await nextTick();
		submitEditForm();
		await flushPromises();

		expect(userRepository.updatedUserRoles).toEqual([{ userId: 2, role: "ADMIN" }]);
		expect(userRepository.updatedUsernames).toEqual([{ userId: 2, username: "jane-new" }]);
		expect(userRepository.updatedUserPasswords).toEqual([]);
		expect(wrapper.text()).toContain("jane-new");
		expect(wrapper.text()).toContain("ADMIN");
		expect(document.body.querySelector("[role='dialog']")).toBeNull();
	});

	it("keeps the modal open and leaves the row unchanged when updating fails", async () => {
		const wrapper = mount(UserList, {
			attachTo: document.body,
			global: {
				provide: {
					[UserRepositoryKey]: new FailingUpdateUsernameRepository(),
				},
			},
		});

		await flushPromises();

		await wrapper.get('[aria-label="Benutzer jane bearbeiten"]').trigger("click");
		await nextTick();
		setInputValue("#edit-username-2", "jane-new");
		await nextTick();
		submitEditForm();
		await flushPromises();

		const alert = document.body.querySelector("[role='alert']");

		expect(alert?.textContent).toBe('Benutzer "jane" konnte nicht aktualisiert werden.');
		expect(wrapper.text()).toContain("jane");
		expect(wrapper.text()).not.toContain("jane-new");
		expect(document.body.querySelector("[role='dialog']")).not.toBeNull();
	});

	it("deletes a user and removes it from the list", async () => {
		const userRepository = new MockUserRepository();
		const wrapper = mount(UserList, {
			attachTo: document.body,
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
		await getDeleteButton(janeRow).trigger("click");
		await flushPromises();

		expect(userRepository.deletedUserIds).toEqual([2]);
		expect(wrapper.text()).not.toContain("jane");
		expect(wrapper.findAll("tbody tr")).toHaveLength(2);
	});

	it("renders an alert when deleting a user fails", async () => {
		const wrapper = mount(UserList, {
			attachTo: document.body,
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
		await getDeleteButton(janeRow).trigger("click");
		await flushPromises();

		const alert = wrapper.get("[role='alert']");

		expect(alert.text()).toBe('Benutzer "jane" konnte nicht gelöscht werden.');
		expect(wrapper.text()).toContain("jane");
	});

	it("renders the empty state when no users are loaded", async () => {
		const wrapper = mount(UserList, {
			attachTo: document.body,
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
			attachTo: document.body,
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
			attachTo: document.body,
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

function getDeleteButton(row: ReturnType<ReturnType<typeof mount>["findAll"]>[number]) {
	const deleteButton = row.findAll("button").find((button) => button.text() === "Löschen");

	if (deleteButton === undefined) {
		throw new Error("Expected delete button to exist");
	}

	return deleteButton;
}

function setInputValue(selector: string, value: string): void {
	const input = document.body.querySelector<HTMLInputElement>(selector);
	if (input === null) {
		throw new Error(`Expected input ${selector} to exist`);
	}

	input.value = value;
	input.dispatchEvent(new Event("input", { bubbles: true }));
}

function setSelectValue(selector: string, value: string): void {
	const select = document.body.querySelector<HTMLSelectElement>(selector);
	if (select === null) {
		throw new Error(`Expected select ${selector} to exist`);
	}

	select.value = value;
	select.dispatchEvent(new Event("change", { bubbles: true }));
}

function submitEditForm(): void {
	const form = document.body.querySelector<HTMLFormElement>(".user-edit-form");
	if (form === null) {
		throw new Error("Expected edit form to exist");
	}

	form.dispatchEvent(new Event("submit", { bubbles: true, cancelable: true }));
}
