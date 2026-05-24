<script setup lang="ts">
import { UserCreateForm, UserList } from "@/feature/user";
import { ref } from "vue";

const refreshKey = ref(0);

function refreshUsers(): void {
	refreshKey.value += 1;
}
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
				<UserList :key="refreshKey" />
			</section>

			<section class="admin-section">
				<h2>Benutzer erstellen</h2>
				<UserCreateForm @created="refreshUsers" />
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
