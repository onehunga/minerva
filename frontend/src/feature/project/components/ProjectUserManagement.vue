<script setup lang="ts">
import { computed, ref, watch } from "vue";
import { EllipsisIcon, PencilIcon, Trash2Icon, UserPlusIcon } from "@lucide/vue";
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
import { Label } from "@/components/ui/label";
import {
	Select,
	SelectContent,
	SelectItem,
	SelectTrigger,
	SelectValue,
} from "@/components/ui/select";
import {
	Sheet,
	SheetContent,
	SheetDescription,
	SheetFooter,
	SheetHeader,
	SheetTitle,
} from "@/components/ui/sheet";
import {
	Table,
	TableBody,
	TableCell,
	TableEmpty,
	TableFooter,
	TableHead,
	TableHeader,
	TableRow,
} from "@/components/ui/table";
import { useUsers, useUserStore } from "@/feature/user";
import type { ProjectRole, ProjectUser } from "../project.model";
import { useProject } from "../composables/useProject";
import { useProjectUsers } from "../composables/useProjectUsers";

const props = withDefaults(
	defineProps<{
		projectId: number;
		disabled?: boolean;
	}>(),
	{ disabled: false },
);

const projectRoles: ProjectRole[] = ["OWNER", "CONTRIBUTOR", "VIEWER"];
const projectRoleLabels: Record<ProjectRole, string> = {
	OWNER: "Owner",
	CONTRIBUTOR: "Mitwirkender",
	VIEWER: "Betrachter",
};

const { details: projectDetails } = useProject();
const { users: allUsers, isLoadingUsers: isLoadingAllUsers } = useUsers();
const userStore = useUserStore();
const {
	addProjectUser,
	hasLoadError,
	isAddingUser,
	isLoadingUsers,
	loadUsers,
	removeProjectUser,
	removingUserId,
	updateProjectUserRole,
	updatingUserRoleId,
	users,
} = useProjectUsers(() => props.projectId);

const errorMessage = ref("");
const successMessage = ref("");
const visibleErrorMessage = computed(
	() =>
		errorMessage.value ||
		(hasLoadError.value ? "Projektbenutzer konnten nicht geladen werden." : ""),
);
const selectedUserId = ref<number | null>(null);
const selectedRole = ref<ProjectRole>("CONTRIBUTOR");
const roleCandidate = ref<ProjectUser | null>(null);
const deleteCandidate = ref<ProjectUser | null>(null);

const isOwner = computed(() => projectDetails.value?.projectRole === "OWNER");
const isAdmin = computed(() => userStore.userDetails?.role === "ADMIN");
const availableUsers = computed(() =>
	allUsers.value.filter(
		(user) => users.value.some((projectUser) => projectUser.id === user.id) === false,
	),
);
const isRoleEditOpen = computed(() => roleCandidate.value !== null);
const availableProjectRoles = computed(() =>
	isOwner.value ? projectRoles : projectRoles.filter((role) => role === "OWNER"),
);
const hasSelectedRoleChanged = computed(
	() =>
		roleCandidate.value?.projectRole !== null &&
		selectedRole.value !== roleCandidate.value?.projectRole,
);

watch(
	() => props.projectId,
	() => {
		clearMessages();
		loadUsers();
	},
	{ immediate: true },
);

function formatProjectRole(role: ProjectRole | null): string {
	return role === null ? "Kein Mitglied" : projectRoleLabels[role];
}

function clearMessages(): void {
	errorMessage.value = "";
	successMessage.value = "";
}

function canUpdateProjectRole(user: ProjectUser): boolean {
	return (
		!props.disabled &&
		isOwner.value &&
		user.id !== userStore.userDetails?.id &&
		user.projectRole !== null
	);
}

function canAssignOwner(user: ProjectUser): boolean {
	return !props.disabled && isAdmin.value && user.projectRole !== "OWNER";
}

function canManageUser(user: ProjectUser): boolean {
	return canUpdateProjectRole(user) || canAssignOwner(user);
}

function selectUser(value: unknown): void {
	selectedUserId.value = Number(value);
}

function selectRole(value: unknown): void {
	selectedRole.value = String(value) as ProjectRole;
}

async function submitProjectUser(): Promise<void> {
	if (props.disabled || selectedUserId.value === null) {
		return;
	}

	clearMessages();
	if (await addProjectUser(selectedUserId.value, selectedRole.value)) {
		selectedUserId.value = null;
		selectedRole.value = "CONTRIBUTOR";
		successMessage.value = "Benutzer wurde hinzugefügt.";
	} else {
		errorMessage.value = "Benutzer konnte nicht hinzugefügt werden.";
	}
}

function openRoleEdit(user: ProjectUser): void {
	if (!canManageUser(user)) {
		return;
	}

	clearMessages();
	roleCandidate.value = user;
	selectedRole.value = canUpdateProjectRole(user) ? user.projectRole! : "OWNER";
}

function updateRoleEditOpen(open: boolean): void {
	if (!open && updatingUserRoleId.value === null) {
		roleCandidate.value = null;
	}
}

async function submitProjectUserRole(): Promise<void> {
	if (props.disabled || roleCandidate.value === null || !hasSelectedRoleChanged.value) {
		return;
	}

	clearMessages();
	if (await updateProjectUserRole(roleCandidate.value.id, selectedRole.value)) {
		roleCandidate.value = null;
		successMessage.value = "Projektrolle wurde aktualisiert.";
	} else {
		errorMessage.value = "Projektrolle konnte nicht aktualisiert werden.";
	}
}

function openDeleteDialog(user: ProjectUser): void {
	if (!canUpdateProjectRole(user)) {
		return;
	}

	clearMessages();
	deleteCandidate.value = user;
}

function updateDeleteOpen(open: boolean): void {
	if (!open && removingUserId.value === null) {
		deleteCandidate.value = null;
	}
}

async function confirmRemove(): Promise<void> {
	if (props.disabled || deleteCandidate.value === null) {
		return;
	}

	clearMessages();
	if (await removeProjectUser(deleteCandidate.value.id)) {
		deleteCandidate.value = null;
		successMessage.value = "Projektmitglied wurde entfernt.";
	} else {
		errorMessage.value = "Projektmitglied konnte nicht entfernt werden.";
	}
}
</script>

<template>
	<section class="flex min-h-0 flex-1 flex-col gap-3">
		<h2 class="m-0 text-lg font-semibold">Projektbenutzer</h2>

		<p v-if="visibleErrorMessage" class="m-0 text-sm text-destructive" role="alert">
			{{ visibleErrorMessage }}
		</p>
		<p v-if="successMessage" class="m-0 text-sm text-muted-foreground" role="status">
			{{ successMessage }}
		</p>
		<p v-if="isLoadingUsers" class="m-0">Projektbenutzer werden geladen...</p>

		<div v-else class="min-h-0 flex-1 overflow-auto">
			<Table class="min-w-lg">
				<TableHeader class="bg-card sticky top-0 z-10">
					<TableRow>
						<TableHead>Benutzername</TableHead>
						<TableHead>Projektrolle</TableHead>
						<TableHead class="w-12">
							<span class="sr-only">Aktionen</span>
						</TableHead>
					</TableRow>
				</TableHeader>

				<TableBody>
					<TableEmpty v-if="users.length === 0" :colspan="3">
						Es sind keine Projektmitglieder vorhanden.
					</TableEmpty>

					<ContextMenu v-for="user in users" :key="user.id">
						<ContextMenuTrigger as-child :disabled="!canManageUser(user)">
							<TableRow>
								<TableCell class="font-medium">{{ user.username }}</TableCell>
								<TableCell>{{ formatProjectRole(user.projectRole) }}</TableCell>
								<TableCell class="text-right">
									<DropdownMenu v-if="canManageUser(user)">
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
												data-action="edit-role"
												@select="openRoleEdit(user)"
											>
												<PencilIcon />
												Projektrolle ändern
											</DropdownMenuItem>
											<DropdownMenuSeparator
												v-if="canUpdateProjectRole(user)"
											/>
											<DropdownMenuItem
												v-if="canUpdateProjectRole(user)"
												data-action="remove"
												variant="destructive"
												@select="openDeleteDialog(user)"
											>
												<Trash2Icon />
												Aus Projekt entfernen
											</DropdownMenuItem>
										</DropdownMenuContent>
									</DropdownMenu>
								</TableCell>
							</TableRow>
						</ContextMenuTrigger>

						<ContextMenuContent>
							<ContextMenuItem data-action="edit-role" @select="openRoleEdit(user)">
								<PencilIcon />
								Projektrolle ändern
							</ContextMenuItem>
							<ContextMenuSeparator v-if="canUpdateProjectRole(user)" />
							<ContextMenuItem
								v-if="canUpdateProjectRole(user)"
								data-action="remove"
								variant="destructive"
								@select="openDeleteDialog(user)"
							>
								<Trash2Icon />
								Aus Projekt entfernen
							</ContextMenuItem>
						</ContextMenuContent>
					</ContextMenu>
				</TableBody>

				<TableFooter v-if="isOwner" class="bg-card sticky bottom-0 z-10">
					<TableRow>
						<TableCell colspan="3">
							<form
								class="flex flex-wrap items-end gap-3"
								@submit.prevent="submitProjectUser"
							>
								<div class="flex min-w-48 flex-1 flex-col gap-2">
									<Label for="project-user">Benutzer</Label>
									<Select
										:model-value="
											selectedUserId === null
												? undefined
												: String(selectedUserId)
										"
										:disabled="
											disabled || availableUsers.length === 0 || isAddingUser
										"
										@update:model-value="selectUser"
									>
										<SelectTrigger id="project-user" class="w-full">
											<SelectValue
												:placeholder="
													availableUsers.length === 0
														? 'Keine Benutzer verfügbar'
														: 'Benutzer auswählen'
												"
											/>
										</SelectTrigger>
										<SelectContent>
											<SelectItem
												v-for="user in availableUsers"
												:key="user.id"
												:value="String(user.id)"
											>
												{{ user.username }}
											</SelectItem>
										</SelectContent>
									</Select>
								</div>

								<div class="flex min-w-48 flex-1 flex-col gap-2">
									<Label for="project-role">Projektrolle</Label>
									<Select
										:model-value="selectedRole"
										:disabled="
											disabled || availableUsers.length === 0 || isAddingUser
										"
										@update:model-value="selectRole"
									>
										<SelectTrigger id="project-role" class="w-full">
											<SelectValue />
										</SelectTrigger>
										<SelectContent>
											<SelectItem
												v-for="role in projectRoles"
												:key="role"
												:value="role"
											>
												{{ formatProjectRole(role) }}
											</SelectItem>
										</SelectContent>
									</Select>
								</div>

								<Button
									type="submit"
									:disabled="
										selectedUserId === null ||
										disabled ||
										availableUsers.length === 0 ||
										isAddingUser
									"
								>
									<UserPlusIcon />
									{{ isAddingUser ? "Wird hinzugefügt..." : "Hinzufügen" }}
								</Button>
							</form>
						</TableCell>
					</TableRow>
				</TableFooter>
			</Table>
		</div>

		<Sheet :open="isRoleEditOpen" @update:open="updateRoleEditOpen">
			<SheetContent side="right" class="sm:max-w-md">
				<SheetHeader>
					<SheetTitle>Projektrolle ändern</SheetTitle>
					<SheetDescription>
						Ändere die Projektrolle von „{{ roleCandidate?.username }}“.
					</SheetDescription>
				</SheetHeader>

				<form class="flex min-h-0 flex-1 flex-col" @submit.prevent="submitProjectUserRole">
					<div class="flex flex-col gap-2 px-4">
						<Label for="edit-project-role">Projektrolle</Label>
						<Select
							:model-value="selectedRole"
							:disabled="disabled || updatingUserRoleId !== null"
							@update:model-value="selectRole"
						>
							<SelectTrigger id="edit-project-role" class="w-full">
								<SelectValue />
							</SelectTrigger>
							<SelectContent v-if="!isLoadingAllUsers">
								<SelectItem
									v-for="role in availableProjectRoles"
									:key="role"
									:value="role"
								>
									{{ formatProjectRole(role) }}
								</SelectItem>
							</SelectContent>
							<SelectContent v-else>
								<SelectItem :value="null" disabled> Lädt... </SelectItem>
							</SelectContent>
						</Select>

						<p v-if="errorMessage" class="m-0 text-sm text-destructive" role="alert">
							{{ errorMessage }}
						</p>
					</div>

					<SheetFooter class="sm:flex-row sm:justify-end">
						<Button
							type="button"
							variant="outline"
							:disabled="updatingUserRoleId !== null"
							@click="roleCandidate = null"
						>
							Abbrechen
						</Button>
						<Button
							type="submit"
							:disabled="
								disabled || updatingUserRoleId !== null || !hasSelectedRoleChanged
							"
						>
							{{
								updatingUserRoleId === null
									? "Rolle speichern"
									: "Wird gespeichert..."
							}}
						</Button>
					</SheetFooter>
				</form>
			</SheetContent>
		</Sheet>

		<AlertDialog :open="deleteCandidate !== null" @update:open="updateDeleteOpen">
			<AlertDialogContent>
				<AlertDialogHeader>
					<AlertDialogTitle>Projektmitglied entfernen?</AlertDialogTitle>
					<AlertDialogDescription>
						„{{ deleteCandidate?.username }}“ verliert den Zugriff auf dieses Projekt.
					</AlertDialogDescription>
				</AlertDialogHeader>
				<p v-if="errorMessage" class="m-0 text-sm text-destructive" role="alert">
					{{ errorMessage }}
				</p>
				<AlertDialogFooter>
					<AlertDialogCancel :disabled="removingUserId !== null">
						Abbrechen
					</AlertDialogCancel>
					<Button
						variant="destructive"
						:disabled="disabled || removingUserId !== null"
						@click="confirmRemove"
					>
						{{ removingUserId === null ? "Aus Projekt entfernen" : "Wird entfernt..." }}
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
