<script setup lang="ts">
import { computed, ref, watch } from "vue";
import type {
	Ticket,
	TicketPriorityName,
	TicketType,
	WorkflowState,
	WorkflowTransition,
} from "../ticket.model";
import { TICKET_PRIORITY_ORDER, TICKET_PRIORITY_LABELS } from "../priority-labels";
import CustomSelect from "@/components/CustomSelect.vue";
import { useProject } from "@/feature/project";
import { ActivityTimeline, useTicketActivities } from "@/feature/activity";
import TicketComments from "./TicketComments.vue";
import CreateTicketForm from "./CreateTicketForm.vue";

const props = defineProps<{
	ticket: Ticket;
	ticketType?: TicketType;
	childTickets: Ticket[];
}>();

const {
	details: projectDetails,
	projectUsers,
	deleteTicket,
	updateTicketStatus,
	updateTicketPriority,
	updateTicketDetails,
	updateTicketAssignee,
} = useProject();

const {
	events: activityEvents,
	isLoading: isActivityLoading,
	errorMessage: activityError,
} = useTicketActivities(props.ticket.projectId, props.ticket.id);

const TICKET_PRIORITIES = TICKET_PRIORITY_ORDER;
const emit = defineEmits<{
	(event: "selectTicket", ticketId: number): void;
}>();

const selectedTransition = ref<WorkflowTransition | null>(null);
const selectedPriority = ref<TicketPriorityName | null>(null);
const isDeletingTicket = ref(false);
const isUpdatingStatus = ref(false);
const isUpdatingPriority = ref(false);
const isUpdatingDetails = ref(false);
const isUpdatingAssignee = ref(false);
const editingField = ref<"name" | "description" | null>(null);
const draftName = ref(props.ticket.name);
const draftDescription = ref(props.ticket.description);

const selectedAssignee = ref<number | null>(props.ticket.assignedTo);
const projectMemberUsers = computed(() => projectUsers.value.filter((user) => user.member));
const possibleAssignees = computed<Array<number | null>>(() => [
	null,
	...projectMemberUsers.value
		.map((user) => user.id)
		.filter((userId) => userId !== props.ticket.assignedTo),
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

const ticketTypeName = computed(
	() => props.ticketType?.name ?? `Ticketart #${props.ticket.ticketTypeId}`,
);

function getStateName(statusId: number): string {
	return (
		props.ticketType?.states.find((state) => state.id === statusId)?.name ??
		`Status #${statusId}`
	);
}

function selectChildTicket(ticketId: number): void {
	emit("selectTicket", ticketId);
}

watch(selectedTransition, () => {
	submitStatusUpdate();
});

watch(selectedPriority, () => {
	submitPriorityUpdate();
});

watch(
	() => [props.ticket.id, props.ticket.name, props.ticket.description],
	() => {
		if (editingField.value == null) {
			draftName.value = props.ticket.name;
			draftDescription.value = props.ticket.description;
		}
	},
);

watch(
	() => props.ticket.assignedTo,
	(assignedTo) => {
		selectedAssignee.value = assignedTo;
	},
);

watch(selectedAssignee, async () => {
	if (selectedAssignee.value === props.ticket.assignedTo) {
		return;
	}

	if (isUpdatingAssignee.value || !canModifyTickets.value) {
		selectedAssignee.value = props.ticket.assignedTo;
		return;
	}

	const assignedTo = selectedAssignee.value;
	const previousAssignee = props.ticket.assignedTo;
	isUpdatingAssignee.value = true;

	try {
		await updateTicketAssignee(props.ticket.id, assignedTo);
	} catch {
		alert("Der Bearbeiter konnte nicht aktualisiert werden.");
		selectedAssignee.value = previousAssignee;
	} finally {
		isUpdatingAssignee.value = false;
	}
});

async function submitStatusUpdate(): Promise<void> {
	if (selectedTransition.value == null) {
		return;
	}

	const transition = availableTransitions.value.find(
		(currentTransition) => currentTransition.id === selectedTransition.value?.id,
	);

	if (transition === undefined) {
		alert("Der gewählte Übergang ist nicht mehr verfügbar.");
		return;
	}

	isUpdatingStatus.value = true;

	try {
		await updateTicketStatus(props.ticket.id, transition.id, transition.toStateId);
		selectedTransition.value = null;
	} catch {
		alert("Der Status konnte nicht aktualisiert werden.");
	} finally {
		isUpdatingStatus.value = false;
	}
}

async function submitDeleteTicket(): Promise<void> {
	if (isDeletingTicket.value || !canModifyTickets.value) {
		return;
	}

	if (!confirm("Ticket wirklich löschen?")) {
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

async function submitPriorityUpdate(): Promise<void> {
	if (selectedPriority.value == null) {
		return;
	}

	if (isUpdatingPriority.value || !canModifyTickets.value) {
		selectedPriority.value = null;
		return;
	}

	const priority = selectedPriority.value;
	selectedPriority.value = null;

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

function formatPriority(priority: TicketPriorityName): string {
	return TICKET_PRIORITY_LABELS[priority];
}

function formatProjectUser(userId: number): string {
	return projectMemberUsers.value.find((user) => user.id === userId)?.username ?? `#${userId}`;
}

function formatAssignee(userId: number | null): string {
	return userId == null ? "Nicht zugewiesen" : formatProjectUser(userId);
}

function formatDate(value: string | null): string {
	if (value == null) {
		return "-";
	}

	return new Intl.DateTimeFormat("de-DE", {
		dateStyle: "medium",
		timeStyle: "short",
	}).format(new Date(value));
}
</script>

<template>
	<article class="ticket-detail">
		<header class="ticket-detail__header">
			<p class="ticket-detail__eyebrow">Ticket #{{ ticket.id }}</p>
			<div v-if="editingField === 'name'" class="ticket-detail__inline-edit">
				<input
					v-model="draftName"
					aria-label="Ticketname"
					:disabled="isUpdatingDetails"
					@keyup.enter="submitDetailsUpdate"
					@keyup.escape="cancelDetailsEdit"
				/>
				<button
					type="button"
					aria-label="Ticketname speichern"
					:disabled="isUpdatingDetails || !draftName.trim()"
					@click="submitDetailsUpdate"
				>
					✓
				</button>
				<button
					type="button"
					aria-label="Ticketname bearbeiten abbrechen"
					:disabled="isUpdatingDetails"
					@click="cancelDetailsEdit"
				>
					×
				</button>
			</div>
			<h3 v-else>
				<button
					v-if="canModifyTickets"
					type="button"
					class="ticket-detail__editable"
					@click="beginDetailsEdit('name')"
				>
					{{ ticket.name }}
				</button>
				<template v-else>{{ ticket.name }}</template>
			</h3>
			<button
				v-if="canModifyTickets"
				type="button"
				class="ticket-detail__delete"
				:disabled="isDeletingTicket"
				@click="submitDeleteTicket"
			>
				Ticket löschen
			</button>
		</header>

		<div v-if="editingField === 'description'" class="ticket-detail__inline-edit">
			<textarea
				v-model="draftDescription"
				aria-label="Ticketbeschreibung"
				:disabled="isUpdatingDetails"
				@keyup.escape="cancelDetailsEdit"
			></textarea>
			<button
				type="button"
				aria-label="Ticketbeschreibung speichern"
				:disabled="isUpdatingDetails || !draftName.trim()"
				@click="submitDetailsUpdate"
			>
				✓
			</button>
			<button
				type="button"
				aria-label="Ticketbeschreibung bearbeiten abbrechen"
				:disabled="isUpdatingDetails"
				@click="cancelDetailsEdit"
			>
				×
			</button>
		</div>
		<p v-else class="ticket-detail__description">
			<button
				v-if="canModifyTickets"
				type="button"
				class="ticket-detail__editable"
				@click="beginDetailsEdit('description')"
			>
				{{ ticket.description || "Keine Beschreibung hinterlegt." }}
			</button>
			<template v-else>{{ ticket.description || "Keine Beschreibung hinterlegt." }}</template>
		</p>

		<label for="ticket-status">Status</label>
		<CustomSelect :options="availableTransitions" v-model="selectedTransition">
			<template #trigger>
				{{ currentStatus?.name }}
			</template>
			<template #option="{ value: transition }">
				{{ transition.name }} → {{ getStateName(transition.toStateId) }}
			</template>
		</CustomSelect>
		<label for="ticket-priority">Priorität</label>
		<CustomSelect
			v-if="canModifyTickets"
			:options="TICKET_PRIORITIES"
			v-model="selectedPriority"
		>
			<template #trigger>
				{{ formatPriority(ticket.priority) }}
			</template>
			<template #option="{ value: priority }">
				{{ formatPriority(priority) }}
			</template>
		</CustomSelect>
		<p v-else id="ticket-priority" class="ticket-detail__readonly-value">
			{{ formatPriority(ticket.priority) }}
		</p>

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
				<dt>Zugewiesen an</dt>
				<dd>
					<CustomSelect
						:options="possibleAssignees"
						v-model="selectedAssignee"
						v-if="canModifyTickets"
					>
						<template #trigger>
							{{ formatAssignee(ticket.assignedTo) }}
						</template>
						<template #option="{ value: userId }">
							{{ formatAssignee(userId) }}
						</template>
					</CustomSelect>
					<p v-else class="ticket-detail__readonly-value">
						{{ formatAssignee(ticket.assignedTo) }}
					</p>
				</dd>
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

		<TicketComments
			:project-id="ticket.projectId"
			:ticket-id="ticket.id"
			:can-create-comment="projectDetails?.projectRole !== 'VIEWER'"
		/>

		<ActivityTimeline
			:events="activityEvents"
			:is-loading="isActivityLoading"
			:error-message="activityError"
		/>

		<section class="ticket-detail__children" aria-labelledby="ticket-children-heading">
			<h4 id="ticket-children-heading">Kindtickets</h4>
			<p v-if="childTickets.length === 0" class="ticket-detail__hint">
				Keine Kindtickets vorhanden.
			</p>
			<ul v-else class="ticket-detail__children-list">
				<li v-for="childTicket in childTickets" :key="childTicket.id">
					<button
						type="button"
						class="ticket-detail__child"
						@click="selectChildTicket(childTicket.id)"
					>
						<span>{{ childTicket.name }}</span>
						<small>#{{ childTicket.id }}</small>
					</button>
				</li>
			</ul>
		</section>

		<section
			v-if="projectDetails?.projectRole !== 'VIEWER'"
			class="ticket-detail__create-child"
			aria-labelledby="create-child-ticket-heading"
		>
			<h4 id="create-child-ticket-heading">Kindticket erstellen</h4>
			<CreateTicketForm
				:parent-ticket-id="ticket.id"
				:parent-ticket-type-id="ticket.ticketTypeId"
			/>
		</section>
	</article>
</template>

<style scoped>
.ticket-detail {
	display: flex;
	flex-direction: column;
	gap: 1rem;
	padding: 1rem;
	border: 1px solid currentColor;
	min-height: 18rem;
	box-sizing: border-box;
}

.ticket-detail__header,
.ticket-detail__description,
.ticket-detail__meta,
.ticket-detail__children h4,
.ticket-detail__create-child h4 {
	margin: 0;
}

.ticket-detail__header {
	display: grid;
	grid-template-columns: minmax(0, 1fr) auto;
	gap: 0.25rem;
	align-items: start;
}

.ticket-detail__header h3,
.ticket-detail__eyebrow {
	margin: 0;
}

.ticket-detail__editable {
	display: inline;
	padding: 0;
	border: 0;
	background: transparent;
	color: inherit;
	font: inherit;
	text-align: left;
	cursor: pointer;
}

.ticket-detail__inline-edit {
	display: flex;
	gap: 0.5rem;
	align-items: start;
}

.ticket-detail__inline-edit input,
.ticket-detail__inline-edit textarea {
	flex: 1;
	min-width: 0;
	padding: 0.45rem 0.55rem;
	border: 1px solid currentColor;
	background: Canvas;
	color: CanvasText;
	font: inherit;
}

.ticket-detail__inline-edit textarea {
	min-height: 5rem;
	resize: vertical;
}

.ticket-detail__eyebrow {
	grid-column: 1 / -1;
	font-size: 0.85rem;
}

.ticket-detail__delete {
	padding: 0.45rem 0.7rem;
	border: 1px solid currentColor;
	background: transparent;
	color: inherit;
	cursor: pointer;
}

.ticket-detail__hint,
.ticket-detail__readonly-value,
.ticket-detail__error {
	margin: 0;
	font-size: 0.9rem;
}

.ticket-detail__error {
	color: #b00020;
}

.ticket-detail__meta {
	display: grid;
	grid-template-columns: repeat(auto-fit, minmax(10rem, 1fr));
	gap: 0.75rem 1rem;
}

.ticket-detail__meta div {
	display: flex;
	flex-direction: column;
	gap: 0.2rem;
}

.ticket-detail__meta dt {
	font-weight: 700;
}

.ticket-detail__meta dd {
	margin: 0;
}

.ticket-detail__children,
.ticket-detail__create-child {
	display: flex;
	flex-direction: column;
	gap: 0.75rem;
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
	display: flex;
	width: 100%;
	justify-content: space-between;
	gap: 0.75rem;
	text-align: left;
	padding: 0.6rem 0.8rem;
	border: 1px solid currentColor;
	background: transparent;
	color: inherit;
	cursor: pointer;
}
</style>
