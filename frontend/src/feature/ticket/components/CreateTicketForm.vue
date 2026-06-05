<script setup lang="ts">
import { computed, ref, watch } from "vue";
import { useTicketRepository } from "../composables/useTicketRepository";
import type { TicketType } from "../ticket.model";

const props = defineProps<{
	projectId: number;
}>();

const repository = useTicketRepository();

const ticketTypes = ref<TicketType[]>([]);
const name = ref("");
const description = ref("");
const selectedTicketTypeId = ref<number | null>(null);
const selectedStatusId = ref<number | null>(null);
const isLoading = ref(false);
const isCreating = ref(false);
const errorMessage = ref("");
const successMessage = ref("");

const selectedTicketType = computed(() =>
	ticketTypes.value.find((ticketType) => ticketType.id === selectedTicketTypeId.value),
);

const availableStates = computed(() => selectedTicketType.value?.states ?? []);

function selectDefaultTicketType(): void {
	selectedTicketTypeId.value = ticketTypes.value[0]?.id ?? null;
}

function selectDefaultStatus(): void {
	const openState = availableStates.value.find((state) => state.category === "OPEN");
	selectedStatusId.value = openState?.id ?? availableStates.value[0]?.id ?? null;
}

async function loadTicketTypes(): Promise<void> {
	isLoading.value = true;
	errorMessage.value = "";
	successMessage.value = "";

	try {
		ticketTypes.value = await repository.getTicketTypes(props.projectId);
		selectDefaultTicketType();
		selectDefaultStatus();
	} catch {
		ticketTypes.value = [];
		selectedTicketTypeId.value = null;
		selectedStatusId.value = null;
		errorMessage.value = "Ticketarten konnten nicht geladen werden.";
	} finally {
		isLoading.value = false;
	}
}

function resetTicketFields(): void {
	name.value = "";
	description.value = "";
	selectDefaultTicketType();
	selectDefaultStatus();
}

async function createTicket(): Promise<void> {
	if (selectedTicketTypeId.value == null || selectedStatusId.value == null || isCreating.value) {
		return;
	}

	isCreating.value = true;
	errorMessage.value = "";
	successMessage.value = "";

	try {
		const ticket = await repository.createTicket(props.projectId, {
			name: name.value,
			description: description.value,
			ticketTypeId: selectedTicketTypeId.value,
			statusId: selectedStatusId.value,
		});

		successMessage.value = `Ticket "${ticket.name}" wurde erstellt.`;
		resetTicketFields();
	} catch {
		errorMessage.value = "Ticket konnte nicht erstellt werden.";
	} finally {
		isCreating.value = false;
	}
}

watch(() => props.projectId, loadTicketTypes, { immediate: true });

watch(selectedTicketTypeId, () => {
	selectDefaultStatus();
});
</script>

<template>
	<form class="create-ticket-form" @submit.prevent="createTicket">
		<p v-if="isLoading">Ticketarten werden geladen...</p>
		<p v-else-if="ticketTypes.length === 0">
			Für dieses Projekt sind keine Ticketarten angelegt.
		</p>

		<template v-else>
			<input
				v-model="name"
				required
				aria-label="Name"
				placeholder="Name"
				:disabled="isCreating"
			/>
			<textarea
				v-model="description"
				aria-label="Beschreibung"
				placeholder="Beschreibung"
				:disabled="isCreating"
			></textarea>

			<select
				v-model.number="selectedTicketTypeId"
				required
				aria-label="Ticketart"
				:disabled="isCreating"
			>
				<option
					v-for="ticketType in ticketTypes"
					:key="ticketType.id"
					:value="ticketType.id"
				>
					{{ ticketType.name }}
				</option>
			</select>

			<select
				v-model.number="selectedStatusId"
				required
				aria-label="Startstatus"
				:disabled="isCreating || availableStates.length === 0"
			>
				<option v-for="state in availableStates" :key="state.id" :value="state.id">
					{{ state.name }}
				</option>
			</select>

			<button
				type="submit"
				:disabled="
					isCreating ||
					!name.trim() ||
					selectedTicketTypeId == null ||
					selectedStatusId == null
				"
			>
				{{ isCreating ? "Ticket wird erstellt..." : "Ticket erstellen" }}
			</button>
		</template>

		<p v-if="errorMessage" class="form-message">{{ errorMessage }}</p>
		<p v-if="successMessage" class="form-message">{{ successMessage }}</p>
	</form>
</template>

<style scoped>
.create-ticket-form {
	display: flex;
	flex-direction: column;
	gap: 0.75rem;
	max-width: 32rem;
}

.create-ticket-form input,
.create-ticket-form select,
.create-ticket-form textarea {
	padding: 0.45rem 0.55rem;
	border: 1px solid currentColor;
	background: Canvas;
	color: CanvasText;
}

.create-ticket-form textarea {
	min-height: 5rem;
	resize: vertical;
}

.create-ticket-form p,
.form-message {
	margin: 0;
}
</style>
