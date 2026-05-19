<script setup lang="ts">
import { onMounted } from "vue";
import { useManageUsers, type model } from "..";

const {
	deleteErrorMessage,
	deletingUserId,
	deleteUser,
	isLoadingUsers,
	loadErrorMessage,
	loadUsers,
	roleErrorMessage,
	updateUserRole,
	updatingRoleUserId,
	users,
} = useManageUsers();

onMounted(loadUsers);

async function changeUserRole(user: model.UserRecord, event: Event): Promise<void> {
	const select = event.target as HTMLSelectElement;
	const wasUpdated = await updateUserRole(user, select.value as model.UserRole);

	if (!wasUpdated) {
		select.value = user.role;
	}
}
</script>

<template>
	<p v-if="isLoadingUsers">Benutzer werden geladen...</p>
	<p v-else-if="loadErrorMessage" role="alert">{{ loadErrorMessage }}</p>
	<p v-else-if="users.length === 0">Es sind noch keine Benutzer vorhanden.</p>

	<template v-else>
		<p v-if="deleteErrorMessage" role="alert">{{ deleteErrorMessage }}</p>
		<p v-if="roleErrorMessage" role="alert">{{ roleErrorMessage }}</p>

		<table class="user-list">
			<thead>
				<tr>
					<th scope="col">Benutzername</th>
					<th scope="col">Rolle</th>
					<th scope="col">Aktionen</th>
				</tr>
			</thead>
			<tbody>
				<tr v-for="user in users" :key="user.id">
					<td>{{ user.username }}</td>
					<td>
						<select
							:aria-label="`Rolle von ${user.username}`"
							:value="user.role"
							:disabled="updatingRoleUserId === user.id"
							@change="changeUserRole(user, $event)"
						>
							<option value="USER">USER</option>
							<option value="ADMIN">ADMIN</option>
						</select>
					</td>
					<td>
						<button
							type="button"
							:disabled="deletingUserId !== null"
							@click="deleteUser(user)"
						>
							{{ deletingUserId === user.id ? "Wird gelöscht..." : "Löschen" }}
						</button>
					</td>
				</tr>
			</tbody>
		</table>
	</template>
</template>

<style scoped>
.user-list {
	width: 100%;
	border-collapse: collapse;
}

.user-list th,
.user-list td {
	padding: 0.5rem;
	text-align: left;
	border-bottom: 1px solid currentColor;
}
</style>
