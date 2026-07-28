<script setup lang="ts">
import { computed, ref } from "vue";
import {
	BanIcon,
	EllipsisIcon,
	PencilIcon,
	RotateCcwIcon,
	Trash2Icon,
	UserPlusIcon,
} from "@lucide/vue";
import {
	AlertDialog,
	AlertDialogCancel,
	AlertDialogContent,
	AlertDialogDescription,
	AlertDialogFooter,
	AlertDialogHeader,
	AlertDialogTitle,
} from "@/components/ui/alert-dialog";
import { Button } from "@/components/ui/button";
import {
	ContextMenu,
	ContextMenuContent,
	ContextMenuItem,
	ContextMenuSeparator,
	ContextMenuTrigger,
} from "@/components/ui/context-menu";
import {
	DropdownMenu,
	DropdownMenuContent,
	DropdownMenuItem,
	DropdownMenuSeparator,
	DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu";
import {
	Sheet,
	SheetContent,
	SheetDescription,
	SheetHeader,
	SheetTitle,
} from "@/components/ui/sheet";
import {
	Table,
	TableBody,
	TableCell,
	TableFooter,
	TableHead,
	TableHeader,
	TableRow,
} from "@/components/ui/table";
import type { UserRecord, UserRole } from "../user.model";
import { useManageUsers } from "../composables/useManageUsers";
import UserCreateForm from "./UserCreateForm.vue";
import UserEditForm from "./UserEditForm.vue";

const props = defineProps<{
	currentUserId?: number;
}>();

const {
	deactivateUser,
	deletingUserId,
	deleteUser,
	errorMessage,
	isLoadingUsers,
	loadUsers,
	reactivateUser,
	updatingUserStateId,
	users,
} = useManageUsers();
const isCreateOpen = ref(false);
const selectedUser = ref<UserRecord | null>(null);
const deleteCandidate = ref<UserRecord | null>(null);
const deactivateCandidate = ref<UserRecord | null>(null);

const isEditOpen = computed(() => selectedUser.value !== null);

function formatRole(role: UserRole): string {
	return role === "ADMIN" ? "Administrator" : "Nutzer";
}

function openEdit(user: UserRecord): void {
	selectedUser.value = user;
}

function closeEdit(): void {
	selectedUser.value = null;
}

function updateEditOpen(open: boolean): void {
	if (!open) {
		closeEdit();
	}
}

function isCurrentUser(user: UserRecord): boolean {
	return user.id === props.currentUserId;
}

async function handleCreated(): Promise<void> {
	isCreateOpen.value = false;
	await loadUsers();
}

function openDeleteDialog(user: UserRecord): void {
	if (user.role === "USER") {
		deleteCandidate.value = user;
	}
}

function updateDeleteOpen(open: boolean): void {
	if (!open && deletingUserId.value === null) {
		deleteCandidate.value = null;
	}
}

async function confirmDelete(): Promise<void> {
	if (deleteCandidate.value === null) {
		return;
	}

	if (await deleteUser(deleteCandidate.value)) {
		deleteCandidate.value = null;
	}
}

function openDeactivateDialog(user: UserRecord): void {
	if (!isCurrentUser(user)) {
		deactivateCandidate.value = user;
	}
}

function updateDeactivateOpen(open: boolean): void {
	if (!open && updatingUserStateId.value === null) {
		deactivateCandidate.value = null;
	}
}

async function confirmDeactivate(): Promise<void> {
	if (deactivateCandidate.value !== null && (await deactivateUser(deactivateCandidate.value))) {
		deactivateCandidate.value = null;
	}
}
</script>

<template>
	<section class="flex min-h-0 flex-1 flex-col gap-3">
		<p v-if="errorMessage" class="m-0 text-sm text-destructive" role="alert">
			{{ errorMessage }}
		</p>
		<p v-if="isLoadingUsers" class="m-0">Benutzer werden geladen...</p>

		<div v-else class="min-h-0 flex-1 overflow-auto">
			<Table class="min-w-lg">
				<TableHeader class="bg-card sticky top-0 z-10">
					<TableRow>
						<TableHead>Benutzername</TableHead>
						<TableHead>Rolle</TableHead>
						<TableHead>Status</TableHead>
						<TableHead class="w-12">
							<span class="sr-only">Aktionen</span>
						</TableHead>
					</TableRow>
				</TableHeader>

				<TableBody>
					<TableRow v-if="users.length === 0">
						<TableCell colspan="4" class="h-24 text-center text-muted-foreground">
							Es sind noch keine Benutzer vorhanden.
						</TableCell>
					</TableRow>

					<ContextMenu v-for="user in users" :key="user.id">
						<ContextMenuTrigger as-child>
							<TableRow>
								<TableCell class="font-medium">{{ user.username }}</TableCell>
								<TableCell>{{ formatRole(user.role) }}</TableCell>
								<TableCell>{{
									user.deactivated ? "Deaktiviert" : "Aktiv"
								}}</TableCell>
								<TableCell class="text-right">
									<DropdownMenu>
										<DropdownMenuTrigger as-child>
											<Button
												variant="ghost"
												size="icon-sm"
												:aria-label="`Aktionen für ${user.username}`"
												@click.stop
											>
												<EllipsisIcon />
											</Button>
										</DropdownMenuTrigger>
										<DropdownMenuContent align="end">
											<DropdownMenuItem
												data-action="edit"
												@select="openEdit(user)"
											>
												<PencilIcon />
												Benutzer bearbeiten
											</DropdownMenuItem>
											<DropdownMenuSeparator />
											<DropdownMenuItem
												v-if="user.deactivated"
												data-action="reactivate"
												:disabled="updatingUserStateId !== null"
												@select="reactivateUser(user)"
											>
												<RotateCcwIcon />
												Benutzer reaktivieren
											</DropdownMenuItem>
											<DropdownMenuItem
												v-else
												data-action="deactivate"
												:disabled="
													isCurrentUser(user) ||
													updatingUserStateId !== null
												"
												@select="openDeactivateDialog(user)"
											>
												<BanIcon />
												Benutzer deaktivieren
											</DropdownMenuItem>
											<DropdownMenuSeparator />
											<DropdownMenuItem
												data-action="delete"
												variant="destructive"
												:disabled="user.role === 'ADMIN'"
												@select="openDeleteDialog(user)"
											>
												<Trash2Icon />
												Benutzer löschen
											</DropdownMenuItem>
										</DropdownMenuContent>
									</DropdownMenu>
								</TableCell>
							</TableRow>
						</ContextMenuTrigger>

						<ContextMenuContent>
							<ContextMenuItem data-action="edit" @select="openEdit(user)">
								<PencilIcon />
								Benutzer bearbeiten
							</ContextMenuItem>
							<ContextMenuSeparator />
							<ContextMenuItem
								v-if="user.deactivated"
								data-action="reactivate"
								:disabled="updatingUserStateId !== null"
								@select="reactivateUser(user)"
							>
								<RotateCcwIcon />
								Benutzer reaktivieren
							</ContextMenuItem>
							<ContextMenuItem
								v-else
								data-action="deactivate"
								:disabled="isCurrentUser(user) || updatingUserStateId !== null"
								@select="openDeactivateDialog(user)"
							>
								<BanIcon />
								Benutzer deaktivieren
							</ContextMenuItem>
							<ContextMenuSeparator />
							<ContextMenuItem
								data-action="delete"
								variant="destructive"
								:disabled="user.role === 'ADMIN'"
								@select="openDeleteDialog(user)"
							>
								<Trash2Icon />
								Benutzer löschen
							</ContextMenuItem>
						</ContextMenuContent>
					</ContextMenu>
				</TableBody>

				<TableFooter class="bg-card sticky bottom-0 z-10">
					<TableRow>
						<TableCell colspan="4" class="p-0">
							<Button
								variant="ghost"
								class="h-11 w-full justify-start rounded-none px-3"
								@click="isCreateOpen = true"
							>
								<UserPlusIcon />
								Nutzer hinzufügen
							</Button>
						</TableCell>
					</TableRow>
				</TableFooter>
			</Table>
		</div>

		<Sheet v-model:open="isCreateOpen">
			<SheetContent side="right" class="sm:max-w-md">
				<SheetHeader>
					<SheetTitle>Nutzer hinzufügen</SheetTitle>
					<SheetDescription>
						Lege einen neuen Nutzer und dessen Rolle an.
					</SheetDescription>
				</SheetHeader>
				<UserCreateForm @created="handleCreated" @cancel="isCreateOpen = false" />
			</SheetContent>
		</Sheet>

		<Sheet :open="isEditOpen" @update:open="updateEditOpen">
			<SheetContent side="right" class="sm:max-w-md">
				<SheetHeader>
					<SheetTitle>Benutzer bearbeiten</SheetTitle>
					<SheetDescription>Ändere Benutzername, Passwort oder Rolle.</SheetDescription>
				</SheetHeader>
				<UserEditForm
					v-if="selectedUser !== null"
					:key="selectedUser.id"
					:user="selectedUser"
					:can-edit-role="!isCurrentUser(selectedUser)"
					@saved="closeEdit"
					@cancel="closeEdit"
				/>
			</SheetContent>
		</Sheet>

		<AlertDialog :open="deleteCandidate !== null" @update:open="updateDeleteOpen">
			<AlertDialogContent>
				<AlertDialogHeader>
					<AlertDialogTitle>Benutzer löschen?</AlertDialogTitle>
					<AlertDialogDescription>
						Der Benutzer „{{ deleteCandidate?.username }}“ wird anonymisiert und kann
						sich anschließend nicht mehr anmelden.
					</AlertDialogDescription>
				</AlertDialogHeader>
				<p v-if="errorMessage" class="m-0 text-sm text-destructive" role="alert">
					{{ errorMessage }}
				</p>
				<AlertDialogFooter>
					<AlertDialogCancel :disabled="deletingUserId !== null">
						Abbrechen
					</AlertDialogCancel>
					<Button
						variant="destructive"
						:disabled="deletingUserId !== null"
						@click="confirmDelete"
					>
						{{ deletingUserId === null ? "Benutzer löschen" : "Wird gelöscht..." }}
					</Button>
				</AlertDialogFooter>
			</AlertDialogContent>
		</AlertDialog>

		<AlertDialog :open="deactivateCandidate !== null" @update:open="updateDeactivateOpen">
			<AlertDialogContent>
				<AlertDialogHeader>
					<AlertDialogTitle>Benutzer deaktivieren?</AlertDialogTitle>
					<AlertDialogDescription>
						Der Benutzer „{{ deactivateCandidate?.username }}“ wird abgemeldet und kann
						sich bis zur Reaktivierung nicht mehr anmelden.
					</AlertDialogDescription>
				</AlertDialogHeader>
				<p v-if="errorMessage" class="m-0 text-sm text-destructive" role="alert">
					{{ errorMessage }}
				</p>
				<AlertDialogFooter>
					<AlertDialogCancel :disabled="updatingUserStateId !== null"
						>Abbrechen</AlertDialogCancel
					>
					<Button
						variant="destructive"
						:disabled="updatingUserStateId !== null"
						@click="confirmDeactivate"
					>
						{{
							updatingUserStateId === null
								? "Benutzer deaktivieren"
								: "Wird deaktiviert..."
						}}
					</Button>
				</AlertDialogFooter>
			</AlertDialogContent>
		</AlertDialog>
	</section>
</template>

<style scoped>
:deep([data-slot="table-container"]) {
	overflow: visible;
}
</style>
