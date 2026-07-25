<script setup lang="ts">
import { computed, reactive, ref, watch } from "vue";
import { useRouter } from "vue-router";
import Button from "@/components/ui/button/Button.vue";
import type { TicketWorkflow } from "@/feature/ticket";
import { useProjectRepository } from "../composables/useProjectRepository";
import { useProjects } from "../composables/useProjects";
import ConfigureTicket from "./create_wizard/ConfigureTicket.vue";
import ProjectDetails from "./create_wizard/ProjectDetails.vue";
import TicketTypes from "./create_wizard/TicketTypes.vue";

type WizardStep =
	| { key: "project"; name: string; description: string }
	| { key: "tickets"; name: string; description: string }
	| { key: `ticket:${string}`; name: string; description: string; ticketName: string };

const repository = useProjectRepository();
const { refreshProjects } = useProjects();
const router = useRouter();

const draft = reactive<{
	name: string;
	description: string;
	tickets: Map<string, TicketWorkflow>;
}>({
	name: "",
	description: "",
	tickets: new Map(),
});

const wizardSteps = computed<WizardStep[]>(() => [
	{
		key: "project",
		name: "Projekt Details",
		description: "Geben Sie die grundlegenden Informationen über Ihr Projekt an.",
	},
	{
		key: "tickets",
		name: "Ticket Typen",
		description: "Definieren Sie die verschiedenen Ticket-Typen für Ihr Projekt.",
	},
	...Array.from(draft.tickets.values()).map((ticket) => ({
		key: `ticket:${ticket.name}` as const,
		name: ticket.name,
		description: `${ticket.name} konfigurieren`,
		ticketName: ticket.name,
	})),
]);

const ticketTypes = computed(() => Array.from(draft.tickets.values()));
const ticketNames = computed(() => Array.from(draft.tickets.keys()));
const stepIdx = ref(0);
const maxStepIdx = ref(0);
const isSubmitting = ref(false);
const errorMessage = ref("");

const currentStep = computed(() => wizardSteps.value[stepIdx.value] ?? wizardSteps.value[0]!);
const currentWorkflow = computed(() => {
	if (currentStep.value.key === "project" || currentStep.value.key === "tickets") {
		return null;
	}

	return draft.tickets.get(currentStep.value.ticketName) ?? null;
});
const canContinue = computed(() => {
	if (currentStep.value.key === "project") {
		return draft.name.trim() !== "";
	}
	if (currentStep.value.key === "tickets") {
		return draft.tickets.size > 0;
	}

	return currentWorkflow.value != null && currentWorkflow.value.states.length > 0;
});

watch(stepIdx, (newStepIdx) => {
	maxStepIdx.value = Math.max(maxStepIdx.value, newStepIdx);
});

function addTicket(ticket: TicketWorkflow) {
	draft.tickets.set(ticket.name, ticket);
}

function openTicket(ticketName: string) {
	const index = wizardSteps.value.findIndex(
		(step) =>
			step.key !== "project" && step.key !== "tickets" && step.ticketName === ticketName,
	);
	if (index !== -1) {
		stepIdx.value = index;
	}
}

function removeTicket(ticketName: string) {
	draft.tickets.delete(ticketName);

	for (const [name, workflow] of draft.tickets) {
		if (workflow.children.includes(ticketName)) {
			draft.tickets.set(name, {
				...workflow,
				children: workflow.children.filter((child) => child !== ticketName),
			});
		}
	}

	maxStepIdx.value = Math.min(maxStepIdx.value, wizardSteps.value.length - 1);
}

function updateTicket(oldName: string, updatedWorkflow: TicketWorkflow) {
	const newName = updatedWorkflow.name.trim();
	if (newName === "" || (newName !== oldName && draft.tickets.has(newName))) {
		return;
	}
	if (newName === oldName) {
		draft.tickets.set(oldName, { ...updatedWorkflow, name: newName });
		return;
	}

	const entries = Array.from(draft.tickets.entries()).map(([name, workflow]) => {
		const nextWorkflow = name === oldName ? { ...updatedWorkflow, name: newName } : workflow;
		return [
			name === oldName ? newName : name,
			{
				...nextWorkflow,
				children: nextWorkflow.children.map((child) =>
					child === oldName ? newName : child,
				),
			},
		] as const;
	});

	draft.tickets = new Map(entries);
}

async function createProject() {
	errorMessage.value = "";

	if (draft.name.trim() === "" || draft.tickets.size === 0) {
		errorMessage.value =
			"Bitte geben Sie einen Projektnamen und mindestens einen Ticket-Typ an.";
		return;
	}

	const incompleteTicket = ticketTypes.value.find((ticket) => ticket.states.length === 0);
	if (incompleteTicket != null) {
		errorMessage.value = `Der Ticket-Typ „${incompleteTicket.name}“ benötigt mindestens einen Zustand.`;
		openTicket(incompleteTicket.name);
		return;
	}

	isSubmitting.value = true;
	try {
		const projectId = await repository.createProject({
			name: draft.name.trim(),
			description: draft.description.trim(),
			ticketConfiguration: { tickets: ticketTypes.value },
		});
		await refreshProjects();
		await router.push({ name: "project", params: { id: projectId } });
	} catch {
		errorMessage.value = "Das Projekt konnte nicht erstellt werden.";
	} finally {
		isSubmitting.value = false;
	}
}
</script>

<template>
	<div class="flex min-w-0 flex-col gap-6">
		<header class="overflow-x-auto pb-1">
			<div class="flex min-w-max items-center">
				<template v-for="(step, index) in wizardSteps" :key="step.key">
					<div
						v-if="index > 0"
						class="mx-2 h-0.5 w-10"
						:class="index <= maxStepIdx ? 'bg-primary' : 'bg-muted'"
					></div>
					<Button
						type="button"
						:variant="index <= maxStepIdx ? 'default' : 'outline'"
						:disabled="index > maxStepIdx + 1 || isSubmitting"
						@click="stepIdx = index"
					>
						{{ step.name }}
					</Button>
				</template>
			</div>
		</header>

		<div>
			<h1 class="text-2xl font-semibold">{{ currentStep.name }}</h1>
			<p class="text-muted-foreground mt-1">{{ currentStep.description }}</p>
		</div>

		<main class="min-w-0">
			<ProjectDetails
				v-if="currentStep.key === 'project'"
				v-model:name="draft.name"
				v-model:description="draft.description"
			/>
			<TicketTypes
				v-else-if="currentStep.key === 'tickets'"
				:tickets="ticketTypes"
				@add="addTicket"
				@open="openTicket"
				@remove="removeTicket"
			/>
			<ConfigureTicket
				v-else-if="currentWorkflow != null"
				:key="currentWorkflow.name"
				:workflow="currentWorkflow"
				:ticket-names="ticketNames"
				@update="updateTicket(currentWorkflow.name, $event)"
			/>
		</main>

		<p v-if="errorMessage" role="alert" class="text-destructive text-sm">
			{{ errorMessage }}
		</p>

		<div class="flex justify-between gap-3">
			<Button
				type="button"
				variant="outline"
				:disabled="stepIdx === 0 || isSubmitting"
				@click="stepIdx--"
			>
				Zurück
			</Button>
			<Button
				v-if="stepIdx < wizardSteps.length - 1"
				type="button"
				:disabled="!canContinue || isSubmitting"
				@click="stepIdx++"
			>
				Weiter
			</Button>
			<Button
				v-else
				type="button"
				:disabled="!canContinue || isSubmitting"
				@click="createProject"
			>
				{{ isSubmitting ? "Projekt wird erstellt..." : "Projekt erstellen" }}
			</Button>
		</div>
	</div>
</template>
