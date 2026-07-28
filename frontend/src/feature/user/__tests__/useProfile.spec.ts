import { flushPromises, mount } from "@vue/test-utils";
import { createPinia } from "pinia";
import { defineComponent } from "vue";
import { describe, expect, it } from "vitest";
import type { UserDetails } from "../user.model";
import { UserRepository, UserRepositoryKey } from "../user.repository";
import { useUserStore } from "../user.store";
import { useProfile } from "../composables/useProfile";

const userDetails: UserDetails = {
	id: 1,
	username: "jane",
	role: "USER",
};

class ProfileUserRepository extends UserRepository {
	updatedUsernames: { userId: number; username: string }[] = [];
	updatedPasswords: { userId: number; password: string }[] = [];

	override async updateUsername(userId: number, username: string): Promise<void> {
		this.updatedUsernames.push({ userId, username });
	}

	override async updateUserPassword(userId: number, password: string): Promise<void> {
		this.updatedPasswords.push({ userId, password });
	}
}

const ProfileHarness = defineComponent({
	setup() {
		return useProfile();
	},
	template: "<div />",
});

describe("useProfile", () => {
	it("updates the username and current user", async () => {
		const { pinia, repository, wrapper } = mountProfile();

		wrapper.vm.username = " jane-new ";
		await wrapper.vm.updateUsername();
		await flushPromises();

		expect(repository.updatedUsernames).toEqual([{ userId: 1, username: "jane-new" }]);
		expect(useUserStore(pinia).userDetails?.username).toBe("jane-new");
		expect(wrapper.vm.usernameSuccess).toBe("Benutzername wurde aktualisiert.");
	});

	it("rejects mismatched passwords", async () => {
		const { repository, wrapper } = mountProfile();

		wrapper.vm.password = "password1";
		wrapper.vm.passwordConfirmation = "password2";
		await wrapper.vm.updatePassword();

		expect(repository.updatedPasswords).toEqual([]);
		expect(wrapper.vm.passwordError).toBe("Die Passwörter stimmen nicht überein.");
	});
});

function mountProfile() {
	const pinia = createPinia();
	const repository = new ProfileUserRepository();
	useUserStore(pinia).setUserDetails(userDetails);
	const wrapper = mount(ProfileHarness, {
		global: {
			plugins: [pinia],
			provide: {
				[UserRepositoryKey]: repository,
			},
		},
	});

	return { pinia, repository, wrapper };
}
