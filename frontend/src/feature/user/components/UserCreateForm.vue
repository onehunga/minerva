<script setup lang="ts">
import { ref } from "vue";
import { useManageUsers, type model } from "..";

const emit = defineEmits<{
	(event: "created"): void;
}>();

const { createUser, errorMessage, isCreatingUser, successMessage } = useManageUsers();

const username = ref("");
const password = ref("");
const role = ref<model.UserRole>("USER");

async function submitUser(): Promise<void> {
	const wasCreated = await createUser(username.value, password.value, role.value);

	if (wasCreated) {
		username.value = "";
		password.value = "";
		role.value = "USER";
		emit("created");
	}
}
</script>

<template>
	<form class="user-create-form" @submit.prevent="submitUser">
		<div class="form-field">
			<label for="username">Benutzername</label>
			<input
				id="username"
				v-model="username"
				name="username"
				required
				autocomplete="username"
			/>
		</div>

		<div class="form-field">
			<label for="password">Passwort</label>
			<input
				id="password"
				v-model="password"
				name="password"
				type="password"
				required
				autocomplete="new-password"
			/>
		</div>

		<div class="form-field">
			<label for="role">Rolle</label>
			<select id="role" v-model="role" name="role">
				<option value="USER">User</option>
				<option value="ADMIN">Admin</option>
			</select>
		</div>

		<p v-if="errorMessage" class="form-message" role="alert">
			{{ errorMessage }}
		</p>
		<p v-if="successMessage" class="form-message">{{ successMessage }}</p>

		<button type="submit" :disabled="isCreatingUser">
			{{ isCreatingUser ? "Wird erstellt..." : "Benutzer erstellen" }}
		</button>
	</form>
</template>

<style scoped>
.user-create-form {
	display: flex;
	flex-direction: column;
	gap: 1rem;
}

.form-field {
	display: flex;
	flex-direction: column;
	gap: 0.25rem;
}

.form-message {
	margin: 0;
}
</style>
