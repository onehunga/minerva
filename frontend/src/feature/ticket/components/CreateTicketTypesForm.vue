<script setup lang="ts">
import { computed, ref } from "vue";
import type {
	useConfigureTickets,
	TicketStatusCategory,
	CreateTicketType,
	CreateWorkflowState,
	TicketDetails,
} from "../index.ts";
import BaseModal from "@/components/BaseModal.vue";
import CustomSelect from "@/components/CustomSelect.vue";
import EditTicketTypeDetails from "./EditTicketTypeDetails.vue";

enum DetailsConfiguration {
	// Standard Details um z.B. Name und Beschreibung zu bearbeiten
	Details,
	States,
	Transitions,
	Children,
}

const STATUS_LABELS: Record<TicketStatusCategory, string> = {
	OPEN: "Offen",
	IN_PROGRESS: "In Bearbeitung",
	COMPLETED: "Abgeschlossen",
};

const STATUS_CATEGORY_OPTIONS = Object.keys(STATUS_LABELS) as TicketStatusCategory[];

const { configureTickets } = defineProps<{
	configureTickets: ReturnType<typeof useConfigureTickets>;
}>();

const showCreateTicketTypeForm = ref(false);

const showCreateTicketStateForm = ref(false);
const newStateName = ref("");
const newStateCategory = ref<TicketStatusCategory>("OPEN");

const WILDCARD_FROM_STATE = null;
const FROM_STATE_UNSELECTED = "__from_state_unselected__";

const showCreateTicketTransitionForm = ref(false);
const newTransitionName = ref("");
const transitionFromState = ref<string | null | typeof FROM_STATE_UNSELECTED>(
	FROM_STATE_UNSELECTED,
);
const transitionToState = ref<string | null>(null);

const showCreateTicketChildTypeForm = ref(false);
const newChildTypeName = ref<string | null>(null);

const activeTicketTypeDetails = ref<CreateTicketType | null>(null);
const activeDetailsConfiguration = ref<DetailsConfiguration>(DetailsConfiguration.States);

const availableTicketStates = computed(() => {
	if (!activeTicketTypeDetails.value) {
		return [];
	}

	const states = Array.from(activeTicketTypeDetails.value.states.values());

	return states;
});

const stateNameOptions = computed(() => availableTicketStates.value.map((state) => state.name));

const fromStateOptions = computed(() => [WILDCARD_FROM_STATE, ...stateNameOptions.value]);

function formatFromState(fromState: string | null): string {
	return fromState ?? "Alle";
}

const possibleTransitionStates = computed(() => {
	if (!activeTicketTypeDetails.value || transitionFromState.value === FROM_STATE_UNSELECTED) {
		return [];
	}

	return possibleToStatesForTransition(
		transitionFromState.value,
		transitionToState.value ?? "",
		"",
	);
});

const availableTicketChildren = computed(() => {
	if (!activeTicketTypeDetails.value) {
		return [];
	}

	const children = Array.from(activeTicketTypeDetails.value.children.values());

	return children;
});

const possibleTicketChildTypes = computed(() => {
	if (!activeTicketTypeDetails.value) {
		return [];
	}

	const allTicketTypes = Array.from(configureTickets.ticketTypes.value.values());
	const possibleChildren = allTicketTypes.filter(
		(ticket) => !activeTicketTypeDetails.value?.children.has(ticket.name),
	);

	return possibleChildren;
});

const possibleTicketChildTypeNames = computed(() =>
	possibleTicketChildTypes.value.map((ticketType) => ticketType.name),
);

function possibleToStatesForTransition(
	fromState: string | null,
	currentToState: string,
	currentTransitionName: string,
): CreateWorkflowState[] {
	if (!activeTicketTypeDetails.value) {
		return [];
	}

	const allStates = Array.from(activeTicketTypeDetails.value.states.values());

	if (fromState == null) {
		const takenToStates = new Set(
			activeTicketTypeDetails.value.transitions
				.filter(
					(transition) =>
						transition.fromState == null && transition.name !== currentTransitionName,
				)
				.map((transition) => transition.toState),
		);

		return allStates.filter(
			(state) => !takenToStates.has(state.name) || state.name === currentToState,
		);
	}

	const takenToStates = new Set(
		activeTicketTypeDetails.value.transitions
			.filter(
				(transition) =>
					transition.fromState === fromState && transition.name !== currentTransitionName,
			)
			.map((transition) => transition.toState),
	);
	takenToStates.add(fromState);

	return allStates.filter(
		(state) => !takenToStates.has(state.name) || state.name === currentToState,
	);
}

function selectTicket(ticket: CreateTicketType) {
	activeTicketTypeDetails.value = ticket;
	activeDetailsConfiguration.value = DetailsConfiguration.Details;
}

function createNewTicketType(details: TicketDetails) {
	const success = configureTickets.createTicketType(details.name, details.description);

	if (!success) {
		alert("Ein Ticket mit diesem Namen existiert bereits.");
		return;
	}

	showCreateTicketTypeForm.value = false;
}

function updateTicketType(details: TicketDetails) {
	if (!activeTicketTypeDetails.value) {
		return;
	}

	activeTicketTypeDetails.value.name = details.name;
	activeTicketTypeDetails.value.description = details.description;
}

function addNewTicketState() {
	if (!newStateName.value || !activeTicketTypeDetails.value) {
		return;
	}

	if (activeTicketTypeDetails.value == null) {
		alert("Es ist kein Ticket ausgewählt.");
		return;
	}

	const success = configureTickets.addNewTicketState(
		activeTicketTypeDetails.value.name,
		newStateName.value,
		newStateCategory.value,
	);

	if (!success) {
		alert("Ein Status mit diesem Namen existiert bereits.");
		return;
	}

	newStateName.value = "";
	showCreateTicketStateForm.value = false;
}

function addNewTicketTransition() {
	if (
		!newTransitionName.value ||
		transitionFromState.value === FROM_STATE_UNSELECTED ||
		transitionToState.value == null ||
		!activeTicketTypeDetails.value
	) {
		return;
	}

	if (activeTicketTypeDetails.value == null) {
		alert("Es ist kein Ticket ausgewählt.");
		return;
	}

	const success = configureTickets.addNewTicketTransition(
		activeTicketTypeDetails.value.name,
		newTransitionName.value,
		transitionFromState.value,
		transitionToState.value,
	);

	if (!success) {
		alert(
			"Diese Übergangskonfiguration existiert bereits oder die angegebenen Zustände sind ungültig.",
		);
		return;
	}

	newTransitionName.value = "";
	transitionFromState.value = FROM_STATE_UNSELECTED;
	transitionToState.value = null;
	showCreateTicketTransitionForm.value = false;
}

function addNewTicketChildType() {
	if (newChildTypeName.value == null) {
		return;
	}

	if (activeTicketTypeDetails.value == null) {
		alert("Es ist kein Ticket ausgewählt.");
		return;
	}

	const success = configureTickets.addNewTicketChild(
		activeTicketTypeDetails.value.name,
		newChildTypeName.value,
	);

	if (!success) {
		alert("Ein Unter-Ticket mit diesem Namen existiert bereits.");
		return;
	}

	newChildTypeName.value = null;
	showCreateTicketChildTypeForm.value = false;
}

function onStateCategoryChange(stateName: string, category: TicketStatusCategory | null) {
	if (category == null || activeTicketTypeDetails.value == null) {
		return;
	}

	configureTickets.updateTicketStateCategory(
		activeTicketTypeDetails.value.name,
		stateName,
		category,
	);
}

function removeState(stateName: string) {
	if (activeTicketTypeDetails.value == null) {
		return;
	}

	const success = configureTickets.removeTicketState(
		activeTicketTypeDetails.value.name,
		stateName,
	);

	if (!success) {
		alert("Dieser Status wird noch in Übergängen verwendet und kann nicht gelöscht werden.");
	}
}

function onTransitionFromChange(transitionName: string, fromState: string | null) {
	if (activeTicketTypeDetails.value == null) {
		return;
	}

	const transition = activeTicketTypeDetails.value.transitions.find(
		(currentTransition) => currentTransition.name === transitionName,
	);
	if (transition == null) {
		return;
	}

	const success = configureTickets.updateTicketTransition(
		activeTicketTypeDetails.value.name,
		transitionName,
		{ fromState, toState: transition.toState },
	);

	if (!success) {
		alert(
			"Diese Übergangskonfiguration existiert bereits oder die angegebenen Zustände sind ungültig.",
		);
	}
}

function onTransitionToChange(transitionName: string, toState: string | null) {
	if (toState == null || activeTicketTypeDetails.value == null) {
		return;
	}

	const transition = activeTicketTypeDetails.value.transitions.find(
		(currentTransition) => currentTransition.name === transitionName,
	);
	if (transition == null) {
		return;
	}

	const success = configureTickets.updateTicketTransition(
		activeTicketTypeDetails.value.name,
		transitionName,
		{ fromState: transition.fromState, toState },
	);

	if (!success) {
		alert(
			"Diese Übergangskonfiguration existiert bereits oder die angegebenen Zustände sind ungültig.",
		);
	}
}

function removeTransition(transitionName: string) {
	if (activeTicketTypeDetails.value == null) {
		return;
	}

	configureTickets.removeTicketTransition(activeTicketTypeDetails.value.name, transitionName);
}

function removeChild(childName: string) {
	if (activeTicketTypeDetails.value == null) {
		return;
	}

	configureTickets.removeTicketChild(activeTicketTypeDetails.value.name, childName);
}
</script>

<template>
	<div class="configure-ticket-types">
		<aside class="ticket-type-list-panel">
			<h2>Tickets</h2>
			<ul class="ticket-type-list">
				<li
					v-for="ticket in configureTickets.ticketTypes.value.values()"
					:key="ticket.name"
				>
					<button
						type="button"
						class="ticket-type-list-item"
						:class="{ selected: activeTicketTypeDetails === ticket }"
						@click="selectTicket(ticket)"
					>
						{{ ticket.name }}
					</button>
					<div v-if="activeTicketTypeDetails === ticket" class="ticket-type-submenu">
						<button
							type="button"
							class="ticket-type-submenu-item"
							:class="{
								selected:
									activeDetailsConfiguration === DetailsConfiguration.States,
							}"
							@click="activeDetailsConfiguration = DetailsConfiguration.States"
						>
							Zustände bearbeiten
						</button>
						<button
							type="button"
							class="ticket-type-submenu-item"
							:class="{
								selected:
									activeDetailsConfiguration === DetailsConfiguration.Transitions,
							}"
							@click="activeDetailsConfiguration = DetailsConfiguration.Transitions"
						>
							Übergänge bearbeiten
						</button>
						<button
							type="button"
							class="ticket-type-submenu-item"
							:class="{
								selected:
									activeDetailsConfiguration === DetailsConfiguration.Children,
							}"
							@click="activeDetailsConfiguration = DetailsConfiguration.Children"
						>
							Untertickets bearbeiten
						</button>
					</div>
				</li>
			</ul>
		</aside>

		<section class="ticket-type-details">
			<div v-if="activeTicketTypeDetails" class="ticket-type-details-content">
				<div
					v-if="activeDetailsConfiguration == DetailsConfiguration.Details"
					class="edit-ticket-details"
				>
					<EditTicketTypeDetails
						:name="activeTicketTypeDetails.name"
						:description="activeTicketTypeDetails.description"
						@submit="updateTicketType"
					/>
				</div>
				<div
					v-else-if="activeDetailsConfiguration == DetailsConfiguration.States"
					class="edit-ticket-states"
				>
					<ul class="configuration-entry-list">
						<li
							v-for="state in availableTicketStates"
							:key="state.name"
							class="configuration-entry"
						>
							<span class="configuration-entry__label">{{ state.name }}</span>
							<div class="configuration-entry__actions">
								<CustomSelect
									:options="STATUS_CATEGORY_OPTIONS"
									:model-value="state.statusCategory"
									@update:model-value="
										(category) => onStateCategoryChange(state.name, category)
									"
								>
									<template #trigger>
										{{ STATUS_LABELS[state.statusCategory] }}
									</template>
									<template #option="{ value: category }">
										{{ STATUS_LABELS[category] }}
									</template>
								</CustomSelect>
								<button type="button" @click="removeState(state.name)">
									Löschen
								</button>
							</div>
						</li>
					</ul>
					<button type="button" @click="showCreateTicketStateForm = true">
						Neuen Status anlegen
					</button>
				</div>
				<div
					v-else-if="activeDetailsConfiguration == DetailsConfiguration.Transitions"
					class="edit-ticket-transitions"
				>
					<ul class="configuration-entry-list">
						<li
							v-for="transition in activeTicketTypeDetails.transitions"
							:key="transition.name"
							class="configuration-entry"
						>
							<span class="configuration-entry__label">{{ transition.name }}</span>
							<div class="configuration-entry__actions">
								<CustomSelect
									:options="fromStateOptions"
									:model-value="transition.fromState"
									@update:model-value="
										(fromState) =>
											onTransitionFromChange(transition.name, fromState)
									"
								>
									<template #trigger>
										{{ formatFromState(transition.fromState) }}
									</template>
									<template #option="{ value: stateName }">
										{{ formatFromState(stateName) }}
									</template>
								</CustomSelect>
								<span>→</span>
								<CustomSelect
									:options="
										possibleToStatesForTransition(
											transition.fromState,
											transition.toState,
											transition.name,
										).map((state) => state.name)
									"
									:model-value="transition.toState"
									@update:model-value="
										(toState) => onTransitionToChange(transition.name, toState)
									"
								>
									<template #trigger>{{ transition.toState }}</template>
									<template #option="{ value: stateName }">
										{{ stateName }}
									</template>
								</CustomSelect>
								<button type="button" @click="removeTransition(transition.name)">
									Löschen
								</button>
							</div>
						</li>
					</ul>

					<button type="button" @click="showCreateTicketTransitionForm = true">
						Neuen Übergang anlegen
					</button>
				</div>
				<div
					v-else-if="activeDetailsConfiguration == DetailsConfiguration.Children"
					class="ticket-type-children"
				>
					<ul class="configuration-entry-list">
						<li
							v-for="child in availableTicketChildren"
							:key="child"
							class="configuration-entry"
						>
							<span class="configuration-entry__label">{{ child }}</span>
							<div class="configuration-entry__actions">
								<button type="button" @click="removeChild(child)">Löschen</button>
							</div>
						</li>
					</ul>

					<button type="button" @click="showCreateTicketChildTypeForm = true">
						Neue Unterticketart anlegen
					</button>
				</div>
			</div>
			<div v-else class="ticket-type-empty-state">
				<h2>Ticket-Details</h2>
				<p>Wähle links ein Ticket aus, um hier die vollständigen Details zu sehen.</p>
			</div>
		</section>
	</div>

	<button type="button" @click="showCreateTicketTypeForm = true">Neue Ticketart anlegen</button>

	<BaseModal :open="showCreateTicketTypeForm" @close="showCreateTicketTypeForm = false">
		<div class="create-ticket-type">
			<EditTicketTypeDetails
				name=""
				description="Ticket Beschreibung"
				submitName="Neues Ticket erstellen"
				@submit="createNewTicketType"
			/>
		</div>
	</BaseModal>

	<BaseModal
		v-if="showCreateTicketStateForm"
		:open="showCreateTicketStateForm"
		@close="showCreateTicketStateForm = false"
	>
		<div class="create-ticket-state">
			<form @submit.prevent="addNewTicketState">
				<input v-model="newStateName" placeholder="Statusname" />
				<CustomSelect
					:options="STATUS_CATEGORY_OPTIONS"
					:model-value="newStateCategory"
					@update:model-value="
						(category) => {
							if (category != null) {
								newStateCategory = category;
							}
						}
					"
				>
					<template #trigger>{{ STATUS_LABELS[newStateCategory] }}</template>
					<template #option="{ value: category }">
						{{ STATUS_LABELS[category] }}
					</template>
				</CustomSelect>
				<button type="submit" :disabled="!newStateName">Neuen Status erstellen</button>
			</form>
		</div>
	</BaseModal>

	<BaseModal
		v-if="showCreateTicketTransitionForm"
		:open="showCreateTicketTransitionForm"
		@close="showCreateTicketTransitionForm = false"
	>
		<div class="create-ticket-transition">
			<form @submit.prevent="addNewTicketTransition">
				<input v-model="newTransitionName" placeholder="Übergangsname" />
				<CustomSelect
					:options="fromStateOptions"
					:model-value="
						transitionFromState === FROM_STATE_UNSELECTED ? null : transitionFromState
					"
					@update:model-value="transitionFromState = $event"
				>
					<template #trigger>
						{{
							transitionFromState === FROM_STATE_UNSELECTED
								? "Von Zustand wählen"
								: formatFromState(transitionFromState)
						}}
					</template>
					<template #option="{ value: stateName }">
						{{ formatFromState(stateName) }}
					</template>
				</CustomSelect>
				<CustomSelect
					:options="possibleTransitionStates.map((state) => state.name)"
					:model-value="transitionToState"
					@update:model-value="transitionToState = $event"
				>
					<template #trigger>
						{{ transitionToState ?? "Zu Zustand wählen" }}
					</template>
					<template #option="{ value: stateName }">
						{{ stateName }}
					</template>
				</CustomSelect>
				<button
					type="submit"
					:disabled="
						!newTransitionName ||
						transitionFromState === FROM_STATE_UNSELECTED ||
						transitionToState == null
					"
				>
					Neuen Übergang erstellen
				</button>
			</form>
		</div>
	</BaseModal>

	<BaseModal
		v-if="showCreateTicketChildTypeForm"
		:open="showCreateTicketChildTypeForm"
		@close="showCreateTicketChildTypeForm = false"
	>
		<div class="create-ticket-child-type">
			<form @submit.prevent="addNewTicketChildType">
				<CustomSelect
					:options="possibleTicketChildTypeNames"
					:model-value="newChildTypeName"
					@update:model-value="newChildTypeName = $event"
				>
					<template #trigger>
						{{ newChildTypeName ?? "Ticketart wählen" }}
					</template>
					<template #option="{ value: childName }">
						{{ childName }}
					</template>
				</CustomSelect>
				<button type="submit" :disabled="newChildTypeName == null">
					Neue Unterticketart erstellen
				</button>
			</form>
		</div>
	</BaseModal>
</template>

<style scoped>
.configure-ticket-types {
	display: grid;
	width: max-content;
	grid-template-columns: minmax(14rem, 18rem) minmax(52rem, 1fr);
	gap: 1rem;
	align-items: stretch;
}

.ticket-type-list-panel {
	display: flex;
	flex-direction: column;
	gap: 0.75rem;
	padding: 1rem;
	border: 1px solid currentColor;
	min-height: 20rem;
	box-sizing: border-box;
}

.ticket-type-list-panel h2 {
	margin: 0;
}

.ticket-type-list {
	margin: 0;
	padding: 0;
	list-style: none;
	display: flex;
	flex-direction: column;
	gap: 0.5rem;
}

.ticket-type-list-item {
	width: 100%;
	text-align: left;
	padding: 0.6rem 0.8rem;
	border: 1px solid currentColor;
	background: transparent;
	cursor: pointer;
}

.ticket-type-list-item.selected {
	font-weight: 700;
	outline: 2px solid currentColor;
	outline-offset: 1px;
}

.ticket-type-submenu {
	display: flex;
	flex-direction: column;
	gap: 0.35rem;
	padding: 0.4rem 0 0 0.9rem;
}

.ticket-type-submenu-item {
	text-align: left;
	padding: 0.4rem 0.6rem;
	border: 1px solid currentColor;
	background: transparent;
	cursor: pointer;
	font-size: 0.9rem;
}

.ticket-type-submenu-item.selected {
	font-weight: 700;
}

.ticket-type-details {
	padding: 1.25rem;
	border: 1px solid currentColor;
	min-height: 20rem;
	min-width: 52rem;
	width: auto;
	box-sizing: border-box;
	display: flex;
	flex-direction: column;
	justify-content: center;
}

.ticket-type-details-content,
.ticket-type-empty-state {
	display: flex;
	flex-direction: column;
	gap: 0.8rem;
}

.ticket-type-details-content h2,
.ticket-type-details-content p,
.ticket-type-empty-state h2,
.ticket-type-empty-state p {
	margin: 0;
}

.configuration-entry-list {
	margin: 0;
	padding: 0;
	list-style: none;
	display: flex;
	flex-direction: column;
	gap: 0.5rem;
}

.configuration-entry {
	display: flex;
	align-items: center;
	justify-content: space-between;
	gap: 0.75rem;
	padding: 0.6rem 0.8rem;
	border: 1px solid currentColor;
}

.configuration-entry__label {
	flex: 1;
	min-width: 0;
}

.configuration-entry__actions {
	display: flex;
	align-items: center;
	gap: 0.5rem;
	flex-shrink: 0;
}

@media (max-width: 70rem) {
	.configure-ticket-types {
		grid-template-columns: 1fr;
	}
}
</style>
