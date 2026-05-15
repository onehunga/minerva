<script setup lang="ts">
import { onMounted, ref } from "vue";
import { useUserRepository, type model } from "..";

const userRepository = useUserRepository();

const users = ref<model.UserRecord[]>([]);
const isLoading = ref(false);
const errorMessage = ref("");

async function loadUsers(): Promise<void> {
	isLoading.value = true;
	errorMessage.value = "";

	try {
		const response = await userRepository.getAllUsers();
		users.value = response.users;
	} catch {
		errorMessage.value = "Benutzer konnten nicht geladen werden.";
	} finally {
		isLoading.value = false;
	}
}

onMounted(loadUsers);
</script>

<template>
	<p v-if="isLoading">Benutzer werden geladen...</p>
	<p v-else-if="errorMessage" role="alert">{{ errorMessage }}</p>
	<p v-else-if="users.length === 0">Es sind noch keine Benutzer vorhanden.</p>

	<table v-else class="user-list">
		<thead>
			<tr>
				<th scope="col">Benutzername</th>
				<th scope="col">Rolle</th>
			</tr>
		</thead>
		<tbody>
			<tr v-for="user in users" :key="user.id">
				<td>{{ user.username }}</td>
				<td>{{ user.role }}</td>
			</tr>
		</tbody>
	</table>
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
