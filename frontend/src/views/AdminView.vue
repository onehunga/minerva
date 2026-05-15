<script setup lang="ts">
import { UserCreateForm, UserList, api } from "@/feature/user";
import { onMounted, ref } from "vue";

const users = ref<api.UserRecord[]>([]);
const isLoading = ref(false);
const errorMessage = ref("");

async function loadUsers(): Promise<void> {
	isLoading.value = true;
	errorMessage.value = "";

	try {
		const response = await api.getAllUsers();
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
	<main class="admin-view">
		<header>
			<h1>Administrator Ansicht</h1>
			<p>Benutzer anzeigen und neue Benutzer anlegen.</p>
		</header>

		<div class="admin-content">
			<section class="admin-section">
				<h2>Benutzer</h2>
				<p v-if="isLoading">Benutzer werden geladen...</p>
				<p v-else-if="errorMessage" role="alert">{{ errorMessage }}</p>
				<UserList v-else :users="users" />
			</section>

			<section class="admin-section">
				<h2>Benutzer erstellen</h2>
				<UserCreateForm @created="loadUsers" />
			</section>
		</div>
	</main>
</template>

<style scoped>
.admin-view {
	max-width: 72rem;
	margin: 0 auto;
	padding: 2rem;
}

.admin-content {
	display: flex;
	flex-direction: column;
	gap: 1.5rem;
}

.admin-section {
	display: flex;
	flex-direction: column;
	gap: 1rem;
	padding: 1rem;
	border: 1px solid currentColor;
}

.admin-section h2,
.admin-section p {
	margin: 0;
}
</style>
