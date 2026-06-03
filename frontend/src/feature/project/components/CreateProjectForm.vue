<script setup lang="ts">
import { ref } from "vue";
import { useProjectRepository } from "..";
import { CreateTicketForm } from "@/feature/ticket";

const repository = useProjectRepository();

const name = ref("");
const description = ref("");

async function createProject() {
	await repository.createProject(name.value, description.value);

	name.value = "";
	description.value = "";
}
</script>

<template>
	<form class="project-create-form" @submit.prevent="createProject">
		<div class="form-field">
			<label for="name">Name:</label>
			<input id="name" v-model="name" required />
		</div>
		<div class="form-field">
			<label for="description">Description:</label>
			<textarea id="description" v-model="description"></textarea>
		</div>
		<CreateTicketForm />
		<button :disabled="true" type="submit">Create Project</button>
	</form>
</template>

<style scoped>
.project-create-form {
	display: flex;
	flex-direction: column;
	gap: 1rem;
}

.form-field {
	display: flex;
	flex-direction: column;
	gap: 0.25rem;
}
</style>
