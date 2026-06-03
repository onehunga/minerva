<script setup lang="ts">
import { computed, ref } from "vue";
import {
	useConfigureTickets,
	type TicketStatusCategory,
	type CreateTicketType,
	type TicketDetails,
} from "..";
import BaseModal from "@/components/BaseModal.vue";
import EditTicketTypeDetails from "./EditTicketTypeDetails.vue";

const DEFAULT_TICKETS: CreateTicketType[] = [
	{
		name: "Standard Ticket",
		description: "Ein Standard-Ticket mit den üblichen Zuständen und Übergängen.",
		states: new Map([
			["Offen", { name: "Offen", statusCategory: "OPEN" }],
			["In Bearbeitung", { name: "In Bearbeitung", statusCategory: "IN_PROGRESS" }],
			["Abgeschlossen", { name: "Abgeschlossen", statusCategory: "CLOSED" }],
		]),
		transitions: [
			{ name: "Start Bearbeitung", fromState: "Offen", toState: "In Bearbeitung" },
			{ name: "Abschließen", fromState: "In Bearbeitung", toState: "Abgeschlossen" },
			{ name: "Wieder öffnen", fromState: "Abgeschlossen", toState: "Offen" },
		],
		children: new Set(),
	},
];

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
	CLOSED: "Abgeschlossen",
};

const configureTickets = useConfigureTickets(DEFAULT_TICKETS);

const showCreateTicketTypeForm = ref(false);

const showCreateTicketStateForm = ref(false);
const newStateName = ref("");
const newStateCategory = ref<TicketStatusCategory>("OPEN");

const showCreateTicketTransitionForm = ref(false);
const newTransitionName = ref("");
const transitionFromState = ref("");
const transitionToState = ref("");

const showCreateTicketChildTypeForm = ref(false);
const newChildTypeName = ref("");

const activeTicketTypeDetails = ref<CreateTicketType | null>(null);
const activeDetailsConfiguration = ref<DetailsConfiguration>(DetailsConfiguration.States);

const availableTicketStates = computed(() => {
	if (!activeTicketTypeDetails.value) {
		return [];
	}

	const states = Array.from(activeTicketTypeDetails.value.states.values());

	return states;
});

const possibleTransitionStates = computed(() => {
	if (!activeTicketTypeDetails.value) {
		return [];
	}

	const allStates = Array.from(activeTicketTypeDetails.value.states.values());
	const takenToStates = new Set(
		activeTicketTypeDetails.value.transitions
			.filter((transition) => transition.fromState == transitionFromState.value)
			.map((transition) => transition.toState),
	);
	takenToStates.add(transitionFromState.value);

	return allStates.filter((state) => !takenToStates.has(state.name));
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
		(ticket) =>
			ticket.name !== activeTicketTypeDetails.value?.name &&
			!activeTicketTypeDetails.value?.children.has(ticket.name),
	);

	return possibleChildren;
});

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
		!transitionFromState.value ||
		!transitionToState.value ||
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
	transitionFromState.value = "";
	transitionToState.value = "";
	showCreateTicketTransitionForm.value = false;
}

function addNewTicketChildType() {
	if (!newChildTypeName.value) {
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

	newChildTypeName.value = "";
	showCreateTicketChildTypeForm.value = false;
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
					<ul>
						<li
							v-for="state in activeTicketTypeDetails.states.values()"
							:key="state.name"
						>
							{{ state.name }} - {{ STATUS_LABELS[state.statusCategory] }}
						</li>
					</ul>
					<button @click="showCreateTicketStateForm = true">Neuen Status anlegen</button>
				</div>
				<div
					v-else-if="activeDetailsConfiguration == DetailsConfiguration.Transitions"
					class="edit-ticket-transitions"
				>
					<div class="current-transitions">
						<div
							class="transition"
							v-for="transition in activeTicketTypeDetails.transitions"
							:key="transition.name"
						>
							{{ transition.name }}: {{ transition.fromState }} →
							{{ transition.toState }}
						</div>
					</div>

					<button @click="showCreateTicketTransitionForm = true">
						Neuen Übergang anlegen
					</button>
				</div>
				<div
					v-else-if="activeDetailsConfiguration == DetailsConfiguration.Children"
					class="ticket-type-children"
				>
					<ul>
						<li v-for="child in availableTicketChildren" :key="child">
							{{ child }}
						</li>
					</ul>

					<button @click="showCreateTicketChildTypeForm = true">
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
		:v-if="showCreateTicketStateForm"
		:open="showCreateTicketStateForm"
		@close="showCreateTicketStateForm = false"
	>
		<div class="create-ticket-state">
			<form @submit.prevent="addNewTicketState">
				<input v-model="newStateName" placeholder="Statusname" />
				<select v-model="newStateCategory">
					<option
						v-for="status in Object.keys(STATUS_LABELS)"
						:key="status"
						:value="status"
					>
						{{ STATUS_LABELS[status as TicketStatusCategory] }}
					</option>
				</select>
				<button type="submit" :disabled="!newStateName">Neuen Status erstellen</button>
			</form>
		</div>
	</BaseModal>

	<BaseModal
		:v-if="showCreateTicketTransitionForm"
		:open="showCreateTicketTransitionForm"
		@close="showCreateTicketTransitionForm = false"
	>
		<div class="create-ticket-transition">
			<form @submit.prevent="addNewTicketTransition">
				<input v-model="newTransitionName" placeholder="Übergangsname" />
				<select v-model="transitionFromState">
					<option
						v-for="state in availableTicketStates"
						:key="state.name"
						:value="state.name"
					>
						{{ state.name }}
					</option>
					<option v-if="availableTicketStates.length === 0" disabled>
						Keine verfügbaren Zustände
					</option>
				</select>
				<select v-model="transitionToState">
					<option
						v-for="state in possibleTransitionStates"
						:key="state.name"
						:value="state.name"
					>
						{{ state.name }}
					</option>
					<option v-if="possibleTransitionStates.length === 0" disabled>
						Keine verfügbaren Zustände
					</option>
				</select>
				<button type="submit" :disabled="!newTransitionName">
					Neuen Übergang erstellen
				</button>
			</form>
		</div>
	</BaseModal>

	<BaseModal
		:v-if="showCreateTicketChildTypeForm"
		:open="showCreateTicketChildTypeForm"
		@close="showCreateTicketChildTypeForm = false"
	>
		<div class="create-ticket-child-type">
			<form @submit.prevent="addNewTicketChildType">
				<select v-model="newChildTypeName">
					<option
						v-for="ticketType in possibleTicketChildTypes"
						:key="ticketType.name"
						:value="ticketType.name"
					>
						{{ ticketType.name }}
					</option>
					<option v-if="possibleTicketChildTypes.length === 0" disabled>
						Keine verfügbaren Ticketarten
					</option>
				</select>
				<button type="submit" :disabled="!newChildTypeName">
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

@media (max-width: 70rem) {
	.configure-ticket-types {
		grid-template-columns: 1fr;
	}
}
</style>
