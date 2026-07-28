<script setup lang="ts">
import { computed, ref, useId, watch } from "vue";
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
import {
	Sheet,
	SheetContent,
	SheetDescription,
	SheetFooter,
	SheetHeader,
	SheetTitle,
	SheetTrigger,
} from "@/components/ui/sheet";
import { Textarea } from "@/components/ui/textarea";
import { useProject } from "@/feature/project";
import { formatTicketPriority, TICKET_PRIORITY_ORDER } from "../priority-labels";
import type { TicketPriorityName } from "../ticket.model";

const props = withDefaults(
	defineProps<{
		parentTicketId: number | null;
		parentTicketTypeId?: number | null;
		disabled?: boolean;
	}>(),
	{ disabled: false },
);

const { details: projectDetails, projectUsers, ticketTypes, tickets, createTicket } = useProject();
const formId = useId();
const isOpen = ref(false);
const name = ref("");
const description = ref("");
const selectedTicketTypeId = ref<number | null>(null);
const selectedStatusId = ref<number | null>(null);
const selectedPriority = ref<TicketPriorityName>("NORMAL");
const selectedAssigneeId = ref<number | null>(null);
const isCreating = ref(false);
const errorMessage = ref("");

const parentTicketTypeId = computed(() => {
	if (props.parentTicketId == null) {
		return null;
	}

	return (
		props.parentTicketTypeId ??
		tickets.value.find((ticket) => ticket.id === props.parentTicketId)?.ticketTypeId ??
		null
	);
});
const availableTicketTypes = computed(() => {
	if (props.parentTicketId == null) {
		return ticketTypes.value;
	}

	const parentType = ticketTypes.value.find(
		(ticketType) => ticketType.id === parentTicketTypeId.value,
	);
	return parentType == null
		? []
		: ticketTypes.value.filter((ticketType) => parentType.children.includes(ticketType.id));
});
const selectedTicketType = computed(() =>
	availableTicketTypes.value.find((ticketType) => ticketType.id === selectedTicketTypeId.value),
);
const availableStates = computed(() => selectedTicketType.value?.states ?? []);
const title = computed(() =>
	props.parentTicketId == null ? "Ticket erstellen" : "Kindticket erstellen",
);
const isDisabled = computed(() => props.disabled || projectDetails.value?.archived === true);
const possibleAssignees = computed(() =>
	projectUsers.value.filter(
		(user) => user.projectRole === "OWNER" || user.projectRole === "CONTRIBUTOR",
	),
);

function selectDefaultTicketType(): void {
	if (
		selectedTicketTypeId.value != null &&
		availableTicketTypes.value.some(
			(ticketType) => ticketType.id === selectedTicketTypeId.value,
		)
	) {
		return;
	}

	selectedTicketTypeId.value = availableTicketTypes.value[0]?.id ?? null;
}

function selectDefaultStatus(): void {
	const openState = availableStates.value.find((state) => state.category === "OPEN");
	selectedStatusId.value = openState?.id ?? availableStates.value[0]?.id ?? null;
}

function selectTicketType(value: unknown): void {
	selectedTicketTypeId.value = Number(value);
}

function selectStatus(value: unknown): void {
	selectedStatusId.value = Number(value);
}

function selectPriority(value: unknown): void {
	selectedPriority.value = String(value) as TicketPriorityName;
}

function selectAssignee(value: unknown): void {
	selectedAssigneeId.value = value === "unassigned" ? null : Number(value);
}

function resetTicketFields(): void {
	name.value = "";
	description.value = "";
	selectedPriority.value = "NORMAL";
	selectedAssigneeId.value = null;
	selectDefaultTicketType();
	selectDefaultStatus();
}

async function create(): Promise<void> {
	if (
		projectDetails.value == null ||
		selectedTicketTypeId.value == null ||
		selectedStatusId.value == null ||
		isCreating.value ||
		isDisabled.value
	) {
		return;
	}

	isCreating.value = true;
	errorMessage.value = "";
	try {
		await createTicket({
			name: name.value.trim(),
			description: description.value.trim(),
			ticketTypeId: selectedTicketTypeId.value,
			statusId: selectedStatusId.value,
			parentTicketId: props.parentTicketId,
			priority: selectedPriority.value,
			assignedTo: selectedAssigneeId.value,
		});
		resetTicketFields();
		isOpen.value = false;
	} catch {
		errorMessage.value = "Ticket konnte nicht erstellt werden.";
	} finally {
		isCreating.value = false;
	}
}

watch(
	availableTicketTypes,
	() => {
		selectDefaultTicketType();
		selectDefaultStatus();
	},
	{ immediate: true },
);
watch(selectedTicketTypeId, selectDefaultStatus);
</script>

<template>
	<Sheet v-model:open="isOpen">
		<SheetTrigger as-child>
			<Button :disabled="isDisabled">{{ title }}</Button>
		</SheetTrigger>
		<SheetContent side="right" class="sm:max-w-lg">
			<SheetHeader>
				<SheetTitle>{{ title }}</SheetTitle>
				<SheetDescription>
					Trage die Ticketdetails ein und wähle Ticketart und Startzustand.
				</SheetDescription>
			</SheetHeader>

			<form class="create-ticket-form" @submit.prevent="create">
				<div class="create-ticket-form__fields">
					<p v-if="projectDetails == null">Ticketarten werden geladen...</p>
					<p v-else-if="availableTicketTypes.length === 0 && parentTicketId != null">
						Für dieses Kindticket sind keine Ticketarten freigegeben.
					</p>
					<p v-else-if="availableTicketTypes.length === 0">
						Für dieses Projekt sind keine Ticketarten angelegt.
					</p>

					<template v-else>
						<div class="create-ticket-form__field">
							<Label :for="`${formId}-name`">Titel</Label>
							<Input
								:id="`${formId}-name`"
								v-model="name"
								required
								placeholder="Titel"
								:disabled="isCreating || isDisabled"
							/>
						</div>

						<div class="create-ticket-form__field">
							<Label :for="`${formId}-description`">Beschreibung</Label>
							<Textarea
								:id="`${formId}-description`"
								v-model="description"
								placeholder="Beschreibung"
								:disabled="isCreating || isDisabled"
							/>
						</div>

						<div class="create-ticket-form__selects">
							<div class="create-ticket-form__field">
								<Label :for="`${formId}-type`">Ticketart</Label>
								<Select
									:model-value="
										selectedTicketTypeId == null
											? undefined
											: String(selectedTicketTypeId)
									"
									:disabled="isCreating || isDisabled"
									@update:model-value="selectTicketType"
								>
									<SelectTrigger :id="`${formId}-type`" class="w-full">
										<SelectValue placeholder="Ticketart wählen" />
									</SelectTrigger>
									<SelectContent>
										<SelectItem
											v-for="ticketType in availableTicketTypes"
											:key="ticketType.id"
											:value="String(ticketType.id)"
										>
											{{ ticketType.name }}
										</SelectItem>
									</SelectContent>
								</Select>
							</div>

							<div class="create-ticket-form__field">
								<Label :for="`${formId}-status`">Startzustand</Label>
								<Select
									:model-value="
										selectedStatusId == null
											? undefined
											: String(selectedStatusId)
									"
									:disabled="
										isCreating || isDisabled || availableStates.length === 0
									"
									@update:model-value="selectStatus"
								>
									<SelectTrigger :id="`${formId}-status`" class="w-full">
										<SelectValue placeholder="Zustand wählen" />
									</SelectTrigger>
									<SelectContent>
										<SelectItem
											v-for="state in availableStates"
											:key="state.id"
											:value="String(state.id)"
										>
											{{ state.name }}
										</SelectItem>
									</SelectContent>
								</Select>
							</div>

							<div class="create-ticket-form__field">
								<Label :for="`${formId}-priority`">Priorität</Label>
								<Select
									:model-value="selectedPriority"
									:disabled="isCreating || isDisabled"
									@update:model-value="selectPriority"
								>
									<SelectTrigger :id="`${formId}-priority`" class="w-full">
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

							<div class="create-ticket-form__field">
								<Label :for="`${formId}-assignee`">Bearbeiter</Label>
								<Select
									:model-value="
										selectedAssigneeId == null
											? 'unassigned'
											: String(selectedAssigneeId)
									"
									:disabled="isCreating || isDisabled"
									@update:model-value="selectAssignee"
								>
									<SelectTrigger :id="`${formId}-assignee`" class="w-full">
										<SelectValue />
									</SelectTrigger>
									<SelectContent>
										<SelectItem value="unassigned">Nicht zugewiesen</SelectItem>
										<SelectItem
											v-for="user in possibleAssignees"
											:key="user.id"
											:value="String(user.id)"
										>
											{{ user.username }}
										</SelectItem>
									</SelectContent>
								</Select>
							</div>
						</div>
					</template>

					<p v-if="errorMessage" role="alert">{{ errorMessage }}</p>
				</div>

				<SheetFooter v-if="availableTicketTypes.length > 0">
					<Button
						type="submit"
						:disabled="
							isCreating ||
							isDisabled ||
							!name.trim() ||
							selectedTicketTypeId == null ||
							selectedStatusId == null
						"
					>
						{{ isCreating ? "Ticket wird erstellt..." : title }}
					</Button>
				</SheetFooter>
			</form>
		</SheetContent>
	</Sheet>
</template>

<style scoped>
.create-ticket-form {
	display: flex;
	flex: 1;
	min-height: 0;
	flex-direction: column;
}

.create-ticket-form__fields,
.create-ticket-form__field {
	display: flex;
	flex-direction: column;
}

.create-ticket-form__fields {
	gap: 1rem;
	overflow-y: auto;
	padding: 0 1rem;
}

.create-ticket-form__field {
	gap: 0.5rem;
}

.create-ticket-form__selects {
	display: grid;
	grid-template-columns: repeat(2, minmax(0, 1fr));
	gap: 0.75rem;
}

.create-ticket-form p {
	margin: 0;
}

@media (max-width: 30rem) {
	.create-ticket-form__selects {
		grid-template-columns: 1fr;
	}
}
</style>
