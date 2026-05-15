<script setup lang="ts">
import { CreateComponent, api } from "@/feature/user";
import { onMounted, ref } from "vue";

const users = ref<api.UserRecord[]>([]);

onMounted(async () => {
	try {
		const response = await api.getAllUsers();
		users.value = response.users;
	} catch (error) {
		alert("Could not load users" + error);
	}
});
</script>

<template>
	<h1>Administrator Ansicht</h1>
	<p>Willkommen im Admin-Bereich. Hier können Sie Benutzer verwalten</p>

	<ul>
		<li v-for="user in users" :key="user.id">{{ user.username }} - {{ user.role }}</li>
	</ul>

	<CreateComponent />
</template>
