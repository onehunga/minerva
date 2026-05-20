<script setup lang="ts">
import { useEditUser } from "../composables/useEditUser";
import type { UserRecord } from "../user.model";

const props = defineProps<{
	user: UserRecord;
}>();

const emit = defineEmits<{
	(event: "cancel"): void;
	(event: "saved"): void;
}>();

const { errorMessage, isUpdatingUser, password, role, updateUser, username } = useEditUser(
	props.user,
);

async function submitUser(): Promise<void> {
	const wasUpdated = await updateUser();

	if (wasUpdated) {
		emit("saved");
	}
}
</script>

<template>
	<form class="user-edit-form" @submit.prevent="submitUser">
		<div class="form-field">
			<label :for="`edit-username-${user.id}`">Benutzername</label>
			<input
				:id="`edit-username-${user.id}`"
				v-model="username"
				name="username"
				required
				autocomplete="username"
			/>
		</div>

		<div class="form-field">
			<label :for="`edit-password-${user.id}`">Passwort</label>
			<input
				:id="`edit-password-${user.id}`"
				v-model="password"
				name="password"
				type="password"
				autocomplete="new-password"
			/>
		</div>

		<div class="form-field">
			<label :for="`edit-role-${user.id}`">Rolle</label>
			<select :id="`edit-role-${user.id}`" v-model="role" name="role">
				<option value="USER">USER</option>
				<option value="ADMIN">ADMIN</option>
			</select>
		</div>

		<p v-if="errorMessage" class="form-message" role="alert">
			{{ errorMessage }}
		</p>

		<div class="form-actions">
			<button type="button" :disabled="isUpdatingUser" @click="emit('cancel')">
				Abbrechen
			</button>
			<button type="submit" :disabled="isUpdatingUser">
				{{ isUpdatingUser ? "Wird gespeichert..." : "Speichern" }}
			</button>
		</div>
	</form>
</template>

<style scoped>
.user-edit-form {
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

.form-actions {
	display: flex;
	justify-content: flex-end;
	gap: 0.5rem;
}
</style>
