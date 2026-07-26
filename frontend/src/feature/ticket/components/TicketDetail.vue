<script setup lang="ts">
import { computed, ref, watch } from "vue";
import type {
	Ticket,
	TicketPriorityName,
	TicketType,
	WorkflowState,
	WorkflowTransition,
} from "../ticket.model";
import { formatTicketPriority, TICKET_PRIORITY_ORDER } from "../priority-labels";
import { Alert, AlertDescription, AlertTitle } from "@/components/ui/alert";
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
import { Separator } from "@/components/ui/separator";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { Textarea } from "@/components/ui/textarea";
import { ActivityTimeline, useTicketActivities } from "@/feature/activity";
import { useProject } from "@/feature/project";
import { formatDate } from "@/lib/date";
import CreateTicketForm from "./CreateTicketForm.vue";
import TicketComments from "./TicketComments.vue";

const props = defineProps<{
	ticket: Ticket;
	ticketType?: TicketType;
	childTickets: Ticket[];
}>();

const emit = defineEmits<{
	selectTicket: [ticketId: number];
}>();

const {
	details: projectDetails,
	projectUsers,
	deleteTicket,
	archiveTicket,
	updateTicketStatus,
	updateTicketPriority,
	updateTicketDetails,
	updateTicketAssignee,
} = useProject();
const {
	events: activityEvents,
	isLoading: isActivityLoading,
	errorMessage: activityError,
} = useTicketActivities(
	() => props.ticket.projectId,
	() => props.ticket.id,
);

const isDeletingTicket = ref(false);
const isArchivingTicket = ref(false);
const isUpdatingStatus = ref(false);
const isUpdatingPriority = ref(false);
const isUpdatingDetails = ref(false);
const isUpdatingAssignee = ref(false);
const editingField = ref<"name" | "description" | null>(null);
const draftName = ref(props.ticket.name);
const draftDescription = ref(props.ticket.description);

const projectMemberUsers = computed(() => projectUsers.value.filter((user) => user.member));
const possibleAssignees = computed<Array<number | null>>(() => [
	null,
	...projectMemberUsers.value.map((user) => user.id),
]);
const canModifyTickets = computed(
	() => projectDetails.value != null && projectDetails.value.projectRole !== "VIEWER",
);
const currentStatus = computed<WorkflowState | undefined>(() =>
	props.ticketType?.states.find((state) => state.id === props.ticket.statusId),
);
const availableTransitions = computed<WorkflowTransition[]>(
	() =>
		props.ticketType?.transitions.filter(
			(transition) =>
				transition.toStateId !== props.ticket.statusId &&
				(transition.fromStateId == null ||
					transition.fromStateId === props.ticket.statusId),
		) ?? [],
);
const canHaveChildTickets = computed(() => (props.ticketType?.children.length ?? 0) > 0);
const ticketTypeName = computed(
	() => props.ticketType?.name ?? `Ticketart #${props.ticket.ticketTypeId}`,
);

watch(
	() => [props.ticket.id, props.ticket.name, props.ticket.description],
	() => {
		if (editingField.value == null) {
			draftName.value = props.ticket.name;
			draftDescription.value = props.ticket.description;
		}
	},
);

function getStateName(statusId: number): string {
	return (
		props.ticketType?.states.find((state) => state.id === statusId)?.name ??
		`Status #${statusId}`
	);
}

function beginDetailsEdit(field: "name" | "description"): void {
	if (!canModifyTickets.value || isUpdatingDetails.value) {
		return;
	}

	draftName.value = props.ticket.name;
	draftDescription.value = props.ticket.description;
	editingField.value = field;
}

function cancelDetailsEdit(): void {
	draftName.value = props.ticket.name;
	draftDescription.value = props.ticket.description;
	editingField.value = null;
}

async function submitDetailsUpdate(): Promise<void> {
	if (isUpdatingDetails.value || !canModifyTickets.value || !draftName.value.trim()) {
		return;
	}

	const name = draftName.value.trim();
	const description = draftDescription.value.trim();
	if (name === props.ticket.name && description === props.ticket.description) {
		editingField.value = null;
		return;
	}

	isUpdatingDetails.value = true;
	try {
		await updateTicketDetails(props.ticket.id, name, description);
		editingField.value = null;
	} catch {
		alert("Die Ticketdetails konnten nicht aktualisiert werden.");
	} finally {
		isUpdatingDetails.value = false;
	}
}

async function submitStatusUpdate(value: unknown): Promise<void> {
	if (isUpdatingStatus.value || !canModifyTickets.value) {
		return;
	}

	const transition = availableTransitions.value.find(
		(currentTransition) => currentTransition.id === Number(value),
	);
	if (transition === undefined) {
		alert("Der gewählte Übergang ist nicht mehr verfügbar.");
		return;
	}

	isUpdatingStatus.value = true;
	try {
		await updateTicketStatus(props.ticket.id, transition.id, transition.toStateId);
	} catch {
		alert("Der Status konnte nicht aktualisiert werden.");
	} finally {
		isUpdatingStatus.value = false;
	}
}

async function submitPriorityUpdate(value: unknown): Promise<void> {
	if (isUpdatingPriority.value || !canModifyTickets.value) {
		return;
	}

	const priority = String(value) as TicketPriorityName;
	if (priority === props.ticket.priority) {
		return;
	}

	isUpdatingPriority.value = true;
	try {
		await updateTicketPriority(props.ticket.id, priority);
	} catch {
		alert("Die Priorität konnte nicht aktualisiert werden.");
	} finally {
		isUpdatingPriority.value = false;
	}
}

async function submitAssigneeUpdate(value: unknown): Promise<void> {
	if (isUpdatingAssignee.value || !canModifyTickets.value) {
		return;
	}

	const assignedTo = value === "unassigned" ? null : Number(value);
	if (assignedTo === props.ticket.assignedTo) {
		return;
	}

	isUpdatingAssignee.value = true;
	try {
		await updateTicketAssignee(props.ticket.id, assignedTo);
	} catch {
		alert("Der Bearbeiter konnte nicht aktualisiert werden.");
	} finally {
		isUpdatingAssignee.value = false;
	}
}

async function submitArchiveTicket(): Promise<void> {
	if (
		isArchivingTicket.value ||
		!canModifyTickets.value ||
		props.ticket.archived ||
		!confirm("Ticket wirklich archivieren?")
	) {
		return;
	}

	isArchivingTicket.value = true;
	try {
		await archiveTicket(props.ticket.id);
	} catch {
		alert("Das Ticket konnte nicht archiviert werden.");
	} finally {
		isArchivingTicket.value = false;
	}
}

async function submitDeleteTicket(): Promise<void> {
	if (isDeletingTicket.value || !canModifyTickets.value || !confirm("Ticket wirklich löschen?")) {
		return;
	}

	isDeletingTicket.value = true;
	try {
		await deleteTicket(props.ticket.id);
	} catch {
		alert("Das Ticket konnte nicht gelöscht werden.");
	} finally {
		isDeletingTicket.value = false;
	}
}

function formatProjectUser(userId: number): string {
	return projectMemberUsers.value.find((user) => user.id === userId)?.username ?? `#${userId}`;
}

function formatAssignee(userId: number | null): string {
	return userId == null ? "Nicht zugewiesen" : formatProjectUser(userId);
}
</script>

<template>
	<article class="ticket-detail">
		<Alert v-if="ticket.archived">
			<AlertTitle>Archiviertes Ticket</AlertTitle>
			<AlertDescription>Dieses Ticket ist archiviert.</AlertDescription>
		</Alert>

		<div class="ticket-detail__layout">
			<div class="ticket-detail__main">
				<header class="ticket-detail__header">
					<p class="ticket-detail__eyebrow">Ticket #{{ ticket.id }}</p>
					<div v-if="editingField === 'name'" class="ticket-detail__inline-edit">
						<Input
							v-model="draftName"
							aria-label="Ticketname"
							:disabled="isUpdatingDetails"
							@keyup.enter="submitDetailsUpdate"
							@keyup.escape="cancelDetailsEdit"
						/>
						<Button
							type="button"
							aria-label="Ticketname speichern"
							:disabled="isUpdatingDetails || !draftName.trim()"
							@click="submitDetailsUpdate"
						>
							Speichern
						</Button>
						<Button
							type="button"
							variant="outline"
							:disabled="isUpdatingDetails"
							@click="cancelDetailsEdit"
						>
							Abbrechen
						</Button>
					</div>
					<h3 v-else>
						<Button
							v-if="canModifyTickets"
							variant="ghost"
							class="ticket-detail__editable"
							@click="beginDetailsEdit('name')"
						>
							{{ ticket.name }}
						</Button>
						<template v-else>{{ ticket.name }}</template>
					</h3>
				</header>

				<div v-if="editingField === 'description'" class="ticket-detail__inline-edit">
					<Textarea
						v-model="draftDescription"
						aria-label="Ticketbeschreibung"
						:disabled="isUpdatingDetails"
						@keyup.escape="cancelDetailsEdit"
					/>
					<Button
						type="button"
						:disabled="isUpdatingDetails || !draftName.trim()"
						@click="submitDetailsUpdate"
					>
						Speichern
					</Button>
					<Button
						type="button"
						variant="outline"
						:disabled="isUpdatingDetails"
						@click="cancelDetailsEdit"
					>
						Abbrechen
					</Button>
				</div>
				<p v-else class="ticket-detail__description">
					<Button
						v-if="canModifyTickets"
						variant="ghost"
						class="ticket-detail__editable"
						@click="beginDetailsEdit('description')"
					>
						{{ ticket.description || "Keine Beschreibung hinterlegt." }}
					</Button>
					<template v-else>
						{{ ticket.description || "Keine Beschreibung hinterlegt." }}
					</template>
				</p>

				<template v-if="canHaveChildTickets">
					<Separator />
					<section
						class="ticket-detail__section"
						aria-labelledby="ticket-children-heading"
					>
						<h4 id="ticket-children-heading">Kindtickets</h4>
						<p v-if="childTickets.length === 0">Keine Kindtickets vorhanden.</p>
						<ul v-else class="ticket-detail__children-list">
							<li v-for="childTicket in childTickets" :key="childTicket.id">
								<Button
									variant="outline"
									class="ticket-detail__child"
									@click="emit('selectTicket', childTicket.id)"
								>
									<span>{{ childTicket.name }}</span>
									<small>#{{ childTicket.id }}</small>
								</Button>
							</li>
						</ul>
					</section>

					<section
						v-if="projectDetails?.projectRole !== 'VIEWER'"
						class="ticket-detail__section"
					>
						<CreateTicketForm
							:parent-ticket-id="ticket.id"
							:parent-ticket-type-id="ticket.ticketTypeId"
						/>
					</section>
				</template>

				<Separator />
				<Tabs default-value="comments">
					<TabsList>
						<TabsTrigger value="comments">Kommentare</TabsTrigger>
						<TabsTrigger value="activities">Aktivitäten</TabsTrigger>
					</TabsList>
					<TabsContent value="comments" class="ticket-detail__tab-content">
						<TicketComments
							:project-id="ticket.projectId"
							:ticket-id="ticket.id"
							:can-create-comment="projectDetails?.projectRole !== 'VIEWER'"
						/>
					</TabsContent>
					<TabsContent value="activities" class="ticket-detail__tab-content">
						<ActivityTimeline
							:events="activityEvents"
							:is-loading="isActivityLoading"
							:error-message="activityError"
						/>
					</TabsContent>
				</Tabs>
			</div>

			<Separator orientation="vertical" class="ticket-detail__separator--desktop" />
			<Separator class="ticket-detail__separator--mobile" />

			<aside class="ticket-detail__sidebar">
				<section class="ticket-detail__section" aria-labelledby="ticket-actions-heading">
					<h4 id="ticket-actions-heading">Aktionen</h4>

					<div class="ticket-detail__field">
						<Label for="ticket-status">Status</Label>
						<Select
							:disabled="
								!canModifyTickets ||
								isUpdatingStatus ||
								availableTransitions.length === 0
							"
							@update:model-value="submitStatusUpdate"
						>
							<SelectTrigger id="ticket-status" class="w-full">
								<SelectValue :placeholder="currentStatus?.name ?? 'Unbekannt'" />
							</SelectTrigger>
							<SelectContent>
								<SelectItem
									v-for="transition in availableTransitions"
									:key="transition.id"
									:value="String(transition.id)"
								>
									{{ transition.name }} → {{ getStateName(transition.toStateId) }}
								</SelectItem>
							</SelectContent>
						</Select>
					</div>

					<div class="ticket-detail__field">
						<Label for="ticket-priority">Priorität</Label>
						<Select
							:model-value="ticket.priority"
							:disabled="!canModifyTickets || isUpdatingPriority"
							@update:model-value="submitPriorityUpdate"
						>
							<SelectTrigger id="ticket-priority" class="w-full">
								<SelectValue />
							</SelectTrigger>
							<SelectContent>
								<SelectItem
									v-for="priority in TICKET_PRIORITY_ORDER"
									:key="priority"
									:value="priority"
								>
									{{ formatTicketPriority(priority) }}
								</SelectItem>
							</SelectContent>
						</Select>
					</div>

					<div class="ticket-detail__field">
						<Label for="ticket-assignee">Bearbeiter</Label>
						<Select
							:model-value="
								ticket.assignedTo == null ? 'unassigned' : String(ticket.assignedTo)
							"
							:disabled="!canModifyTickets || isUpdatingAssignee"
							@update:model-value="submitAssigneeUpdate"
						>
							<SelectTrigger id="ticket-assignee" class="w-full">
								<SelectValue />
							</SelectTrigger>
							<SelectContent>
								<SelectItem
									v-for="userId in possibleAssignees"
									:key="userId ?? 'unassigned'"
									:value="userId == null ? 'unassigned' : String(userId)"
								>
									{{ formatAssignee(userId) }}
								</SelectItem>
							</SelectContent>
						</Select>
					</div>

					<Button
						v-if="canModifyTickets && !ticket.archived"
						variant="outline"
						:disabled="isArchivingTicket"
						@click="submitArchiveTicket"
					>
						{{ isArchivingTicket ? "Wird archiviert..." : "Ticket archivieren" }}
					</Button>
					<Button
						v-if="canModifyTickets"
						variant="destructive"
						:disabled="isDeletingTicket"
						@click="submitDeleteTicket"
					>
						{{ isDeletingTicket ? "Wird gelöscht..." : "Ticket löschen" }}
					</Button>
				</section>

				<Separator />

				<section class="ticket-detail__section" aria-labelledby="ticket-meta-heading">
					<h4 id="ticket-meta-heading">Eckdaten</h4>
					<dl class="ticket-detail__meta">
						<div>
							<dt>Ticketart</dt>
							<dd>{{ ticketTypeName }}</dd>
						</div>
						<div>
							<dt>Projekt</dt>
							<dd>#{{ ticket.projectId }}</dd>
						</div>
						<div>
							<dt>Erstellt von</dt>
							<dd>{{ formatProjectUser(ticket.createdBy) }}</dd>
						</div>
						<div>
							<dt>Erstellt am</dt>
							<dd>{{ formatDate(ticket.createdAt) }}</dd>
						</div>
						<div>
							<dt>Aktualisiert am</dt>
							<dd>{{ formatDate(ticket.updatedAt) }}</dd>
						</div>
					</dl>
				</section>
			</aside>
		</div>
	</article>
</template>

<style scoped>
.ticket-detail,
.ticket-detail__main,
.ticket-detail__sidebar,
.ticket-detail__section,
.ticket-detail__field,
.ticket-detail__meta,
.ticket-detail__meta div {
	display: flex;
	flex-direction: column;
}

.ticket-detail {
	gap: 1rem;
}

.ticket-detail__layout {
	display: grid;
	grid-template-columns: minmax(0, 1fr) auto minmax(14rem, 18rem);
	gap: 1.5rem;
}

.ticket-detail__main,
.ticket-detail__sidebar {
	gap: 1.25rem;
}

.ticket-detail__header,
.ticket-detail__header h3,
.ticket-detail__header p,
.ticket-detail__description,
.ticket-detail__section h4,
.ticket-detail__section p,
.ticket-detail__meta {
	margin: 0;
}

.ticket-detail__eyebrow {
	color: var(--muted-foreground);
	font-size: 0.875rem;
}

.ticket-detail__editable {
	height: auto;
	max-width: 100%;
	justify-content: flex-start;
	padding: 0;
	white-space: normal;
	font: inherit;
	text-align: left;
}

.ticket-detail__inline-edit {
	display: flex;
	flex-wrap: wrap;
	gap: 0.5rem;
	align-items: start;
}

.ticket-detail__inline-edit > :first-child {
	flex: 1 1 16rem;
}

.ticket-detail__section,
.ticket-detail__field,
.ticket-detail__meta,
.ticket-detail__meta div {
	gap: 0.5rem;
}

.ticket-detail__children-list {
	display: flex;
	flex-direction: column;
	gap: 0.5rem;
	margin: 0;
	padding: 0;
	list-style: none;
}

.ticket-detail__child {
	width: 100%;
	justify-content: space-between;
}

.ticket-detail__tab-content {
	padding-top: 0.75rem;
}

.ticket-detail__meta dt {
	font-weight: 600;
}

.ticket-detail__meta dd {
	margin: 0;
	color: var(--muted-foreground);
}

.ticket-detail__separator--mobile {
	display: none;
}

@media (max-width: 64rem) {
	.ticket-detail__layout {
		grid-template-columns: minmax(0, 1fr);
	}

	.ticket-detail__separator--desktop {
		display: none;
	}

	.ticket-detail__separator--mobile {
		display: block;
	}
}
</style>
