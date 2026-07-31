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
import { Alert, AlertDescription } from "@/components/ui/alert";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Skeleton } from "@/components/ui/skeleton";
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
const searchQuery = ref("");

const isEditOpen = computed(() => selectedUser.value !== null);
const filteredUsers = computed(() =>
	users.value.filter((user) =>
		user.username.toLocaleLowerCase().includes(searchQuery.value.trim().toLocaleLowerCase()),
	),
);

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
		<Input
			v-model="searchQuery"
			type="search"
			placeholder="Benutzer suchen..."
			class="max-w-sm"
		/>
		<Alert v-if="errorMessage" variant="destructive">
			<AlertDescription>{{ errorMessage }}</AlertDescription>
		</Alert>
		<div class="min-h-0 flex-1 overflow-auto">
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
					<template v-if="isLoadingUsers">
						<TableRow v-for="row in 3" :key="row" aria-hidden="true">
							<TableCell v-for="column in 4" :key="column">
								<Skeleton class="h-5 w-full max-w-32" />
							</TableCell>
						</TableRow>
					</template>
					<TableRow v-else-if="filteredUsers.length === 0">
						<TableCell colspan="4" class="h-24 text-center text-muted-foreground">
							{{
								users.length === 0
									? "Es sind noch keine Benutzer vorhanden."
									: "Keine passenden Benutzer gefunden."
							}}
						</TableCell>
					</TableRow>

					<ContextMenu v-for="user in filteredUsers" v-else :key="user.id">
						<ContextMenuTrigger as-child>
							<TableRow>
								<TableCell class="font-medium">{{ user.username }}</TableCell>
								<TableCell>
									<span
										class="inline-flex rounded-full px-2 py-0.5 text-xs font-medium"
										:class="
											user.role === 'ADMIN'
												? 'bg-primary text-primary-foreground'
												: 'bg-muted text-muted-foreground'
										"
									>
										{{ formatRole(user.role) }}
									</span>
								</TableCell>
								<TableCell>
									<span
										class="inline-flex rounded-full px-2 py-0.5 text-xs font-medium"
										:class="
											user.deactivated
												? 'bg-destructive/10 text-destructive'
												: 'bg-emerald-500/15 text-emerald-700 dark:text-emerald-400'
										"
									>
										{{ user.deactivated ? "Deaktiviert" : "Aktiv" }}
									</span>
								</TableCell>
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

				<TableFooter v-if="!isLoadingUsers" class="bg-card sticky bottom-0 z-10">
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
				<Alert v-if="errorMessage" variant="destructive">
					<AlertDescription>{{ errorMessage }}</AlertDescription>
				</Alert>
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
				<Alert v-if="errorMessage" variant="destructive">
					<AlertDescription>{{ errorMessage }}</AlertDescription>
				</Alert>
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
