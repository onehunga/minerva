<script setup lang="ts">
import { onMounted } from "vue";
import { useManageUsers } from "..";

const {
	deleteErrorMessage,
	deletingUserId,
	deleteUser,
	isLoadingUsers,
	loadErrorMessage,
	loadUsers,
	users,
} = useManageUsers();

onMounted(loadUsers);
</script>

<template>
	<p v-if="isLoadingUsers">Benutzer werden geladen...</p>
	<p v-else-if="loadErrorMessage" role="alert">{{ loadErrorMessage }}</p>
	<p v-else-if="users.length === 0">Es sind noch keine Benutzer vorhanden.</p>

	<template v-else>
		<p v-if="deleteErrorMessage" role="alert">{{ deleteErrorMessage }}</p>

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
					<td>{{ user.role }}</td>
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
