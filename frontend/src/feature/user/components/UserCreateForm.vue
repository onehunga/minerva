<script setup lang="ts">
import { ref } from "vue";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { SheetFooter } from "@/components/ui/sheet";
import { useManageUsers } from "..";

const emit = defineEmits<{
	cancel: [];
	created: [];
}>();

const { createUser, errorMessage, isCreatingUser } = useManageUsers();

const username = ref("");
const password = ref("");

async function submitUser(): Promise<void> {
	const wasCreated = await createUser(username.value, password.value, "USER");

	if (wasCreated) {
		username.value = "";
		password.value = "";
		emit("created");
	}
}
</script>

<template>
	<form class="flex min-h-0 flex-1 flex-col" @submit.prevent="submitUser">
		<div class="flex flex-col gap-4 overflow-y-auto px-4">
			<div class="flex flex-col gap-2">
				<Label for="username">Benutzername</Label>
				<Input
					id="username"
					v-model="username"
					name="username"
					required
					minlength="3"
					maxlength="50"
					pattern="[A-Za-z0-9._-]+"
					autocomplete="username"
					:disabled="isCreatingUser"
				/>
			</div>

			<div class="flex flex-col gap-2">
				<Label for="password">Passwort</Label>
				<Input
					id="password"
					v-model="password"
					name="password"
					type="password"
					required
					minlength="8"
					autocomplete="new-password"
					:disabled="isCreatingUser"
				/>
			</div>

			<p v-if="errorMessage" class="m-0 text-sm text-destructive" role="alert">
				{{ errorMessage }}
			</p>
		</div>

		<SheetFooter class="sm:flex-row sm:justify-end">
			<Button
				type="button"
				variant="outline"
				:disabled="isCreatingUser"
				@click="emit('cancel')"
			>
				Abbrechen
			</Button>
			<Button type="submit" :disabled="isCreatingUser">
				{{ isCreatingUser ? "Wird erstellt..." : "Benutzer erstellen" }}
			</Button>
		</SheetFooter>
	</form>
</template>
