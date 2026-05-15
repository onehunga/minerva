<script lang="ts" setup>
import { ref } from "vue";

const username = ref("");
const password = ref("");
const role = ref<api.UserRole>("USER");

import { api } from "../index";

function login() {
	api.createUser(username.value, password.value, role.value)
		.then(() => {
			alert("User created successfully");
			username.value = "";
			password.value = "";
			role.value = "USER";
		})
		.catch((error) => {
			alert("Error creating user: " + error);
		});
}
</script>

<template>
	<div>
		<span>Data:</span>
		{{ username }} - {{ password }} - {{ role }}
	</div>

	<form @submit.prevent="login">
		<h2>Create User</h2>
		<div>
			<label for="name">Name:</label>
			<input type="text" id="name" v-model="username" />
		</div>
		<div>
			<label for="password">Password:</label>
			<input type="password" id="password" v-model="password" />
		</div>
		<div>
			<label for="role">Role:</label>
			<select id="role" v-model="role">
				<option value="USER">User</option>
				<option value="ADMIN">Admin</option>
			</select>
		</div>
		<button type="submit">Create User</button>
	</form>
</template>
