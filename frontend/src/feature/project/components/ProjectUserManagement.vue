<script setup lang="ts">
import { computed, onMounted, ref } from "vue";
import { type model, useProjectUsers } from "..";

const props = defineProps<{
	projectId: number;
}>();

const {
	addProjectUser,
	errorMessage,
	isAddingUser,
	isLoadingUsers,
	loadUsers,
	successMessage,
	users,
} = useProjectUsers(props.projectId);

const selectedUserId = ref<number | null>(null);
const selectedRole = ref<model.ProjectRole>("CONTRIBUTOR");

const availableUsers = computed(() => users.value.filter((user) => !user.member));

onMounted(loadUsers);

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
		<p v-else-if="users.length === 0">Es sind keine Benutzer vorhanden.</p>

		<template v-else>
			<p v-if="errorMessage" role="alert">{{ errorMessage }}</p>
			<p v-if="successMessage">{{ successMessage }}</p>

			<table class="project-user-list">
				<thead>
					<tr>
						<th scope="col">Benutzername</th>
						<th scope="col">Projektrolle</th>
					</tr>
				</thead>
				<tbody>
					<tr v-for="user in users" :key="user.id">
						<td>{{ user.username }}</td>
						<td>{{ formatProjectRole(user.projectRole) }}</td>
					</tr>
				</tbody>
			</table>

			<form class="project-user-form" @submit.prevent="submitProjectUser">
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
