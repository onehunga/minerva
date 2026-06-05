<script setup lang="ts">
import { ref } from "vue";
import { useProjectRepository } from "..";
import {
	CreateTicketTypesForm,
	useConfigureTickets,
	type CreateTicketType,
	type WorkflowConfiguration,
} from "@/feature/ticket";

const DEFAULT_TICKETS: CreateTicketType[] = [
	{
		name: "Standard Ticket",
		description: "Ein Standard-Ticket mit den üblichen Zuständen und Übergängen.",
		states: new Map([
			["Offen", { name: "Offen", statusCategory: "OPEN" }],
			["In Bearbeitung", { name: "In Bearbeitung", statusCategory: "IN_PROGRESS" }],
			["Abgeschlossen", { name: "Abgeschlossen", statusCategory: "COMPLETED" }],
		]),
		transitions: [
			{ name: "Start Bearbeitung", fromState: "Offen", toState: "In Bearbeitung" },
			{ name: "Abschließen", fromState: "In Bearbeitung", toState: "Abgeschlossen" },
			{ name: "Wieder öffnen", fromState: "Abgeschlossen", toState: "Offen" },
		],
		children: new Set(),
	},
];

const repository = useProjectRepository();

const name = ref("");
const description = ref("");

const configureTickets = useConfigureTickets(DEFAULT_TICKETS);

const projectFormId = "project-create-form";

function buildTicketWorkflow(): WorkflowConfiguration {
	return {
		tickets: Array.from(configureTickets.ticketTypes.value.values()).map((ticketType) => {
			const states = Array.from(ticketType.states.values()).map((state) => ({
				name: state.name,
				category: state.statusCategory,
			}));

			const transitions = ticketType.transitions.map((transition) => ({
				name: transition.name,
				from: transition.fromState,
				to: transition.toState,
			}));

			return {
				name: ticketType.name,
				description: ticketType.description,
				states,
				transitions,
				children: Array.from(ticketType.children),
			};
		}),
	};
}

async function createProject() {
	const ticketWorkflow = buildTicketWorkflow();

	await repository.createProject(name.value, description.value, ticketWorkflow);

	name.value = "";
	description.value = "";
}
</script>

<template>
	<div class="project-create-form">
		<form :id="projectFormId" class="project-details-form" @submit.prevent="createProject">
			<div class="form-field">
				<label for="name">Name:</label>
				<input id="name" v-model="name" required />
			</div>
			<div class="form-field">
				<label for="description">Description:</label>
				<textarea id="description" v-model="description"></textarea>
			</div>
		</form>

		<CreateTicketTypesForm :configure-tickets="configureTickets" />

		<button type="submit" :form="projectFormId">Create Project</button>
	</div>
</template>

<style scoped>
.project-create-form {
	display: flex;
	flex-direction: column;
	gap: 1rem;
}

.project-details-form {
	display: flex;
	flex-direction: column;
	gap: 1rem;
}

.form-field {
	display: flex;
	flex-direction: column;
	gap: 0.25rem;
}
</style>
