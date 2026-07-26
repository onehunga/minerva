<script setup lang="ts">
import { ref } from "vue";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import {
	Select,
	SelectContent,
	SelectItem,
	SelectTrigger,
	SelectValue,
} from "@/components/ui/select";
import { SheetFooter } from "@/components/ui/sheet";
import type { UserRole } from "../user.model";
import { useManageUsers } from "../composables/useManageUsers";

const emit = defineEmits<{
	cancel: [];
	created: [];
}>();

const { createUser, errorMessage, isCreatingUser } = useManageUsers();

const username = ref("");
const password = ref("");
const role = ref<UserRole>("USER");

async function submitUser(): Promise<void> {
	const wasCreated = await createUser(username.value, password.value, role.value);

	if (wasCreated) {
		username.value = "";
		password.value = "";
		role.value = "USER";
		emit("created");
	}
}

function selectRole(value: unknown): void {
	role.value = String(value) as UserRole;
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

			<div class="flex flex-col gap-2">
				<Label for="role">Rolle</Label>
				<Select
					:model-value="role"
					:disabled="isCreatingUser"
					@update:model-value="selectRole"
				>
					<SelectTrigger id="role" class="w-full">
						<SelectValue />
					</SelectTrigger>
					<SelectContent>
						<SelectItem value="USER">Nutzer</SelectItem>
						<SelectItem value="ADMIN">Administrator</SelectItem>
					</SelectContent>
				</Select>
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
