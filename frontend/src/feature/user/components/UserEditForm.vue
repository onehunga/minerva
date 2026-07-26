<script setup lang="ts">
import type { UserRole } from "../user.model";
import { useEditUser } from "../composables/useEditUser";
import type { UserRecord } from "../user.model";
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

const props = defineProps<{
	user: UserRecord;
	canEditRole: boolean;
}>();

const emit = defineEmits<{
	cancel: [];
	saved: [];
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

function selectRole(value: unknown): void {
	role.value = String(value) as UserRole;
}
</script>

<template>
	<form class="user-edit-form flex min-h-0 flex-1 flex-col" @submit.prevent="submitUser">
		<div class="flex flex-col gap-4 overflow-y-auto px-4">
			<div class="flex flex-col gap-2">
				<Label :for="`edit-username-${user.id}`">Benutzername</Label>
				<Input
					:id="`edit-username-${user.id}`"
					v-model="username"
					name="username"
					required
					minlength="3"
					maxlength="50"
					pattern="[A-Za-z0-9._-]+"
					autocomplete="username"
					:disabled="isUpdatingUser"
				/>
			</div>

			<div class="flex flex-col gap-2">
				<Label :for="`edit-password-${user.id}`">Neues Passwort (optional)</Label>
				<Input
					:id="`edit-password-${user.id}`"
					v-model="password"
					name="password"
					type="password"
					minlength="8"
					autocomplete="new-password"
					:disabled="isUpdatingUser"
				/>
			</div>

			<div class="flex flex-col gap-2">
				<Label :for="`edit-role-${user.id}`">Rolle</Label>
				<Select
					:model-value="role"
					:disabled="isUpdatingUser || !canEditRole"
					@update:model-value="selectRole"
				>
					<SelectTrigger :id="`edit-role-${user.id}`" class="w-full">
						<SelectValue />
					</SelectTrigger>
					<SelectContent>
						<SelectItem value="USER">Nutzer</SelectItem>
						<SelectItem value="ADMIN">Administrator</SelectItem>
					</SelectContent>
				</Select>
			</div>

			<p v-if="errorMessage.length > 0" class="m-0 text-sm text-destructive" role="alert">
				<template v-for="(message, index) in errorMessage" :key="index">
					<br v-if="index > 0" />
					{{ message }}
				</template>
			</p>
		</div>

		<SheetFooter class="sm:flex-row sm:justify-end">
			<Button
				type="button"
				variant="outline"
				:disabled="isUpdatingUser"
				@click="emit('cancel')"
			>
				Abbrechen
			</Button>
			<Button type="submit" :disabled="isUpdatingUser">
				{{ isUpdatingUser ? "Wird gespeichert..." : "Speichern" }}
			</Button>
		</SheetFooter>
	</form>
</template>
