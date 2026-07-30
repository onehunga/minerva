import { afterEach, describe, expect, it } from "vitest";
import { enableAutoUnmount, flushPromises, mount, type VueWrapper } from "@vue/test-utils";
import { nextTick } from "vue";
import type { UserRecord, UserRecordList } from "../user.model";
import { UserRepository, UserRepositoryKey } from "../user.repository";
import UserList from "../components/UserList.vue";

enableAutoUnmount(afterEach);

const testUsers: UserRecord[] = [
	{
		id: 1,
		username: "admin",
		role: "ADMIN",
		deactivated: false,
	},
	{
		id: 2,
		username: "jane",
		role: "USER",
		deactivated: false,
	},
	{
		id: 3,
		username: "max",
		role: "USER",
		deactivated: true,
	},
];

class MockUserRepository extends UserRepository {
	deletedUserIds: number[] = [];
	deactivatedUserIds: number[] = [];
	reactivatedUserIds: number[] = [];

	override async getAllUsers(): Promise<UserRecordList> {
		return {
			users: testUsers.map((user) => ({ ...user })),
		};
	}

	override async deleteUser(userId: number): Promise<void> {
		this.deletedUserIds.push(userId);
	}

	override async deactivateUser(userId: number): Promise<void> {
		this.deactivatedUserIds.push(userId);
	}

	override async reactivateUser(userId: number): Promise<void> {
		this.reactivatedUserIds.push(userId);
	}
}

class EmptyUserRepository extends UserRepository {
	override async getAllUsers(): Promise<UserRecordList> {
		return {
			users: [],
		};
	}
}

class FailingUserRepository extends UserRepository {
	override async getAllUsers(): Promise<UserRecordList> {
		throw new Error("Failed to load users");
	}
}

class PendingUserRepository extends UserRepository {
	override async getAllUsers(): Promise<UserRecordList> {
		return new Promise<UserRecordList>(() => {});
	}
}

class FailingDeleteUserRepository extends MockUserRepository {
	override async deleteUser(): Promise<void> {
		throw new Error("Failed to delete user");
	}
}

describe("UserList", () => {
	it("renders all users in the table with localized roles", async () => {
		const wrapper = mountUserList(new MockUserRepository());

		await flushPromises();

		expect(wrapper.findAll("tbody tr")).toHaveLength(testUsers.length);
		expect(wrapper.text()).toContain("admin");
		expect(wrapper.text()).toContain("Administrator");
		expect(wrapper.text()).toContain("jane");
		expect(wrapper.text()).toContain("Nutzer");
		expect(wrapper.text()).toContain("Aktiv");
		expect(wrapper.text()).toContain("Deaktiviert");
	});

	it("filters users by username", async () => {
		const wrapper = mountUserList(new MockUserRepository());
		await flushPromises();

		await wrapper.get("input[type='search']").setValue("JAN");

		expect(wrapper.findAll("tbody tr")).toHaveLength(1);
		expect(wrapper.text()).toContain("jane");
		expect(wrapper.text()).not.toContain("admin");
	});

	it("provides an options button and a context menu for every user", async () => {
		const wrapper = mountUserList(new MockUserRepository());

		await flushPromises();

		expect(wrapper.findAll("[aria-label^='Aktionen für']")).toHaveLength(testUsers.length);

		const janeRow = wrapper.findAll("tbody tr")[1];
		if (janeRow === undefined) {
			throw new Error("Expected jane row to exist");
		}

		await janeRow.trigger("contextmenu", { clientX: 10, clientY: 10 });
		await nextTick();

		const menu = document.body.querySelector("[data-slot='context-menu-content']");
		expect(menu?.textContent).toContain("Benutzer bearbeiten");
		expect(menu?.textContent).toContain("Benutzer löschen");
	});

	it("opens the combined edit sheet from the options menu", async () => {
		const wrapper = mountUserList(new MockUserRepository());

		await flushPromises();
		await openActions(wrapper, "jane");
		clickMenuAction("edit");
		await nextTick();

		const sheet = document.body.querySelector("[data-slot='sheet-content']");
		const input = document.body.querySelector<HTMLInputElement>("#edit-username-2");

		expect(sheet?.textContent).toContain("Benutzer bearbeiten");
		expect(input?.value).toBe("jane");
		expect(document.body.querySelector("#edit-password-2")).not.toBeNull();
		expect(document.body.querySelector("#edit-role-2")).not.toBeNull();
	});

	it("disables changing the current admin role in the edit sheet", async () => {
		const wrapper = mountUserList(new MockUserRepository(), 1);

		await flushPromises();
		await openActions(wrapper, "admin");
		clickMenuAction("edit");
		await nextTick();

		expect(document.body.querySelector("#edit-role-1")?.hasAttribute("disabled")).toBe(true);
		expect(document.body.querySelector<HTMLInputElement>("#edit-username-1")?.disabled).toBe(
			false,
		);
	});

	it("opens the create sheet from the final table row", async () => {
		const wrapper = mountUserList(new MockUserRepository());

		await flushPromises();
		await wrapper.get("tfoot button").trigger("click");
		await nextTick();

		const sheet = document.body.querySelector("[data-slot='sheet-content']");
		expect(sheet?.textContent).toContain("Nutzer hinzufügen");
		expect(document.body.querySelector("#username")).not.toBeNull();
		expect(document.body.querySelector("#password")).not.toBeNull();
		expect(document.body.querySelector("#role")).not.toBeNull();
	});

	it("confirms deletion before removing a user", async () => {
		const userRepository = new MockUserRepository();
		const wrapper = mountUserList(userRepository);

		await flushPromises();
		await openActions(wrapper, "jane");
		clickMenuAction("delete");
		await nextTick();

		const dialog = document.body.querySelector("[data-slot='alert-dialog-content']");
		expect(dialog?.textContent).toContain("jane");
		expect(userRepository.deletedUserIds).toEqual([]);

		clickDialogButton("Benutzer löschen");
		await flushPromises();

		expect(userRepository.deletedUserIds).toEqual([2]);
		expect(wrapper.text()).not.toContain("jane");
	});

	it("keeps the deletion dialog open when deleting fails", async () => {
		const wrapper = mountUserList(new FailingDeleteUserRepository());

		await flushPromises();
		await openActions(wrapper, "jane");
		clickMenuAction("delete");
		await nextTick();
		clickDialogButton("Benutzer löschen");
		await flushPromises();

		const dialog = document.body.querySelector("[data-slot='alert-dialog-content']");
		expect(dialog?.textContent).toContain('Benutzer "jane" konnte nicht gelöscht werden.');
		expect(wrapper.text()).toContain("jane");
	});

	it("deactivates and reactivates users from the existing menu", async () => {
		const userRepository = new MockUserRepository();
		const wrapper = mountUserList(userRepository, 1);
		await flushPromises();

		await openActions(wrapper, "jane");
		clickMenuAction("deactivate");
		await nextTick();
		clickDialogButton("Benutzer deaktivieren");
		await flushPromises();

		expect(userRepository.deactivatedUserIds).toEqual([2]);
		expect(wrapper.findAll("tbody tr")[1]?.text()).toContain("Deaktiviert");

		await openActions(wrapper, "max");
		clickMenuAction("reactivate");
		await flushPromises();

		expect(userRepository.reactivatedUserIds).toEqual([3]);
		expect(wrapper.findAll("tbody tr")[2]?.text()).toContain("Aktiv");
	});

	it("renders the empty table with the add action", async () => {
		const wrapper = mountUserList(new EmptyUserRepository());

		await flushPromises();

		expect(wrapper.text()).toContain("Es sind noch keine Benutzer vorhanden.");
		expect(wrapper.find("table").exists()).toBe(true);
		expect(wrapper.text()).toContain("Nutzer hinzufügen");
	});

	it("renders an alert when loading users fails", async () => {
		const wrapper = mountUserList(new FailingUserRepository());

		await flushPromises();

		expect(wrapper.get("[role='alert']").text()).toBe("Benutzer konnten nicht geladen werden.");
		expect(wrapper.find("table").exists()).toBe(true);
	});

	it("renders a loading state while users are loading", async () => {
		const wrapper = mountUserList(new PendingUserRepository());

		await nextTick();

		expect(wrapper.text()).toContain("Benutzer werden geladen...");
		expect(wrapper.find("table").exists()).toBe(false);
	});
});

function mountUserList(userRepository: UserRepository, currentUserId?: number): VueWrapper {
	return mount(UserList, {
		attachTo: document.body,
		props: {
			currentUserId,
		},
		global: {
			provide: {
				[UserRepositoryKey]: userRepository,
			},
		},
	});
}

async function openActions(wrapper: VueWrapper, username: string): Promise<void> {
	await wrapper.get(`[aria-label="Aktionen für ${username}"]`).trigger("click");
	await nextTick();
}

function getDropdownAction(action: string): HTMLElement {
	const element = document.body.querySelector<HTMLElement>(
		`[data-slot='dropdown-menu-content'] [data-action='${action}']`,
	);
	if (element === null) {
		throw new Error(`Expected dropdown action ${action}`);
	}

	return element;
}

function clickMenuAction(action: string): void {
	getDropdownAction(action).click();
}

function clickDialogButton(label: string): void {
	const dialog = document.body.querySelector("[data-slot='alert-dialog-content']");
	const button = Array.from(dialog?.querySelectorAll("button") ?? []).find(
		(candidate) => candidate.textContent?.trim() === label,
	);
	if (button === undefined) {
		throw new Error(`Expected dialog button ${label}`);
	}

	button.click();
}
