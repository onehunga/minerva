<script setup lang="ts">
import type { UserRecord } from "../user.api";

defineProps<{
	users: UserRecord[];
}>();
</script>

<template>
	<p v-if="users.length === 0">Es sind noch keine Benutzer vorhanden.</p>

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
