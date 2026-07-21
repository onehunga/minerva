<script setup lang="ts">
import BaseModal from "@/components/BaseModal.vue";
import { onMounted, ref } from "vue";
import { useManageUsers } from "..";
import type { UserRecord } from "../user.model";
import UserEditForm from "./UserEditForm.vue";

const { deletingUserId, deleteUser, errorMessage, isLoadingUsers, loadUsers, users } =
	useManageUsers();

const selectedUser = ref<UserRecord | null>(null);

onMounted(loadUsers);

function openEditModal(user: UserRecord): void {
	selectedUser.value = user;
}

function closeEditModal(): void {
	selectedUser.value = null;
}
</script>

<template>
	<p v-if="isLoadingUsers">Benutzer werden geladen...</p>
	<p v-else-if="errorMessage && users.length === 0" role="alert">{{ errorMessage }}</p>
	<p v-else-if="users.length === 0">Es sind noch keine Benutzer vorhanden.</p>

	<template v-else>
		<p v-if="errorMessage" role="alert">{{ errorMessage }}</p>

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
							:aria-label="`Benutzer ${user.username} bearbeiten`"
							@click="openEditModal(user)"
						>
							...
						</button>
						<button
							v-if="user.role === 'USER'"
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

	<BaseModal :open="selectedUser !== null" @close="closeEditModal">
		<template #title>Benutzer bearbeiten</template>

		<UserEditForm
			v-if="selectedUser !== null"
			:user="selectedUser"
			@saved="closeEditModal"
			@cancel="closeEditModal"
		/>
	</BaseModal>
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

.user-list td:last-child {
	display: flex;
	gap: 0.5rem;
}
</style>
