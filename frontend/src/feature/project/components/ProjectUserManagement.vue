<script setup lang="ts">
import { computed, onMounted, ref } from "vue";
import { type model, useProject, useProjectUsers } from "..";
import { useUserStore } from "@/feature/user";

const { details: projectDetails } = useProject();

const userStore = useUserStore();
const {
	addProjectUser,
	errorMessage,
	isAddingUser,
	isLoadingUsers,
	loadUsers,
	removeProjectUser,
	removingUserId,
	successMessage,
	updateProjectUserRole,
	updatingUserRoleId,
	users,
} = useProjectUsers(projectDetails.value!.id);

const selectedUserId = ref<number | null>(null);
const selectedRole = ref<model.ProjectRole>("CONTRIBUTOR");
const roleChanges = ref<Record<number, model.ProjectRole>>({});

const isOwner = computed(() => projectDetails.value?.projectRole === "OWNER");
const memberUsers = computed(() => users.value.filter((user) => user.member));
const availableUsers = computed(() => users.value.filter((user) => !user.member));

onMounted(loadProjectUsers);

async function loadProjectUsers(): Promise<void> {
	await loadUsers();
	syncRoleChanges();
}

function syncRoleChanges(): void {
	roleChanges.value = users.value.reduce<Record<number, model.ProjectRole>>((roles, user) => {
		if (user.projectRole !== null) {
			roles[user.id] = user.projectRole;
		}

		return roles;
	}, {});
}

function formatProjectRole(role: model.ProjectRole | null): string {
	if (role === null) {
		return "Kein Mitglied";
	}

	return role;
}

async function submitProjectUser(): Promise<void> {
	if (selectedUserId.value === null) {
		return;
	}

	const wasAdded = await addProjectUser(selectedUserId.value, selectedRole.value);

	if (wasAdded) {
		selectedUserId.value = null;
		selectedRole.value = "CONTRIBUTOR";
		syncRoleChanges();
	}
}

function canUpdateProjectRole(user: model.ProjectUser): boolean {
	return (
		isOwner.value &&
		user.member &&
		user.id !== userStore.userDetails?.id &&
		user.projectRole !== null
	);
}

function hasRoleChanged(user: model.ProjectUser): boolean {
	return user.projectRole !== null && roleChanges.value[user.id] !== user.projectRole;
}

async function submitProjectUserRole(user: model.ProjectUser): Promise<void> {
	const role = roleChanges.value[user.id];

	if (role === undefined) {
		return;
	}

	const wasUpdated = await updateProjectUserRole(user.id, role);

	if (wasUpdated) {
		syncRoleChanges();
	}
}

async function removeUser(user: model.ProjectUser): Promise<void> {
	if (!confirm(`Projektmitglied ${user.username} wirklich entfernen?`)) {
		return;
	}

	if (await removeProjectUser(user.id)) {
		syncRoleChanges();
	}
}
</script>

<template>
	<section class="project-user-management">
		<h2>Projektbenutzer</h2>

		<p v-if="isLoadingUsers">Projektbenutzer werden geladen...</p>
		<p v-else-if="errorMessage && users.length === 0" role="alert">
			{{ errorMessage }}
		</p>
		<p v-else-if="memberUsers.length === 0">Es sind keine Projektmitglieder vorhanden.</p>

		<template v-else>
			<p v-if="errorMessage" role="alert">{{ errorMessage }}</p>
			<p v-if="successMessage">{{ successMessage }}</p>

			<table class="project-user-list">
				<thead>
					<tr>
						<th scope="col">Benutzername</th>
						<th scope="col">Projektrolle</th>
						<th scope="col">Aktionen</th>
					</tr>
				</thead>
				<tbody>
					<tr v-for="user in memberUsers" :key="user.id">
						<td>{{ user.username }}</td>
						<td>
							<select
								v-if="canUpdateProjectRole(user)"
								v-model="roleChanges[user.id]"
								:aria-label="`Projektrolle für ${user.username}`"
								:disabled="updatingUserRoleId !== null || removingUserId !== null"
							>
								<option value="OWNER">OWNER</option>
								<option value="CONTRIBUTOR">CONTRIBUTOR</option>
								<option value="VIEWER">VIEWER</option>
							</select>
							<span v-else>{{ formatProjectRole(user.projectRole) }}</span>
						</td>
						<td>
							<button
								v-if="canUpdateProjectRole(user)"
								type="button"
								:disabled="
									!hasRoleChanged(user) ||
									updatingUserRoleId !== null ||
									removingUserId !== null
								"
								@click="submitProjectUserRole(user)"
							>
								{{
									updatingUserRoleId === user.id
										? "Wird gespeichert..."
										: "Speichern"
								}}
							</button>
							<button
								v-if="canUpdateProjectRole(user)"
								type="button"
								:disabled="updatingUserRoleId !== null || removingUserId !== null"
								@click="removeUser(user)"
							>
								{{ removingUserId === user.id ? "Wird entfernt..." : "Entfernen" }}
							</button>
						</td>
					</tr>
				</tbody>
			</table>

			<form v-if="isOwner" class="project-user-form" @submit.prevent="submitProjectUser">
				<div class="form-field">
					<label for="project-user">Benutzer</label>
					<select
						id="project-user"
						v-model.number="selectedUserId"
						name="project-user"
						:disabled="availableUsers.length === 0 || isAddingUser"
						required
					>
						<option :value="null" disabled>Benutzer auswählen</option>
						<option v-for="user in availableUsers" :key="user.id" :value="user.id">
							{{ user.username }}
						</option>
					</select>
				</div>

				<div class="form-field">
					<label for="project-role">Projektrolle</label>
					<select
						id="project-role"
						v-model="selectedRole"
						name="project-role"
						:disabled="availableUsers.length === 0 || isAddingUser"
					>
						<option value="OWNER">OWNER</option>
						<option value="CONTRIBUTOR">CONTRIBUTOR</option>
						<option value="VIEWER">VIEWER</option>
					</select>
				</div>

				<button
					type="submit"
					:disabled="
						selectedUserId === null || availableUsers.length === 0 || isAddingUser
					"
				>
					{{ isAddingUser ? "Wird hinzugefügt..." : "Hinzufügen" }}
				</button>
			</form>
		</template>
	</section>
</template>

<style scoped>
.project-user-management {
	display: flex;
	flex-direction: column;
	gap: 1rem;
}

.project-user-management h2,
.project-user-management p {
	margin: 0;
}

.project-user-list {
	width: 100%;
	border-collapse: collapse;
}

.project-user-list th,
.project-user-list td {
	padding: 0.5rem;
	text-align: left;
	border-bottom: 1px solid currentColor;
}

.project-user-form {
	display: flex;
	flex-wrap: wrap;
	gap: 1rem;
	align-items: flex-end;
}

.form-field {
	display: flex;
	flex: 1 1 12rem;
	flex-direction: column;
	gap: 0.25rem;
}
</style>
