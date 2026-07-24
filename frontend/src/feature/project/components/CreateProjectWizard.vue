<script setup lang="ts">
import { ref, shallowRef, watch, type Component } from "vue";
import ProjectDetails from "./create_wizard/ProjectDetails.vue";
import Button from "@/components/ui/button/Button.vue";
import TicketTypes from "./create_wizard/TicketTypes.vue";
import ConfigureTicket from "./create_wizard/ConfigureTicket.vue";
import { type model } from "..";

type WizardStep = {
	name: string;
	description: string;
	component: Component;
	props: Record<string, unknown> | undefined;
};

const name = ref<string>("");
const description = ref<string>("");

const ticketTypes = ref<model.TicketTypeDetails[]>([]);

const wizardSteps = shallowRef<WizardStep[]>([
	{
		name: "Projekt Details",
		description: "Geben Sie die grundlegenden Informationen über Ihr Projekt an.",
		component: ProjectDetails,
		props: {
			onStep: (projectName: string, projectDescription: string) => {
				name.value = projectName;
				description.value = projectDescription;
			},
		},
	},
	{
		name: "Ticket Typen",
		description: "Definieren Sie die verschiedenen Ticket-Typen für Ihr Projekt.",
		component: TicketTypes,
		props: {
			ticketNames: ticketTypes.value,
			onStep: (ticketDetail: model.TicketTypeDetails) => {
				ticketTypes.value.push(ticketDetail);

				wizardSteps.value = [
					...wizardSteps.value,
					{
						name: ticketDetail.name,
						description: ticketDetail.description,
						component: ConfigureTicket,
						props: {
							name: ticketDetail,
						},
					},
				];
			},
		},
	},
]);

const stepIdx = ref<number>(0);
const maxStepIdx = ref<number>(0);
watch(
	() => stepIdx.value,
	(newStepIdx) => {
		if (newStepIdx > maxStepIdx.value) {
			maxStepIdx.value = newStepIdx;
		}
	},
);
</script>

<template>
	<header class="wizard-header">
		<div v-for="(step, i) in wizardSteps" :key="i" class="wizard-step">
			<div
				v-if="i != 0"
				class="wizard-step-separator"
				:class="{
					'wizard-step-separator-completed': maxStepIdx >= i,
					'wizard-step-separator-open': maxStepIdx < i,
				}"
			></div>
			<Button
				@click="if (i <= maxStepIdx + 1) stepIdx = i;"
				:variant="i <= maxStepIdx ? 'default' : 'outline'"
				>{{ step.name }}</Button
			>
		</div>
	</header>

	<main>
		<component :is="wizardSteps[stepIdx]!.component" v-bind="wizardSteps[stepIdx]!.props" />
	</main>

	<div class="wizard-controls">
		<Button v-if="stepIdx > 0" @click="stepIdx--"> Zurück </Button>
		<Button v-if="stepIdx < wizardSteps.length - 1" @click="stepIdx++"> Weiter </Button>
		<Button v-else @click="stepIdx = 0"> Fertig </Button>
	</div>
</template>

<style scoped>
.wizard-header {
	display: flex;
	flex-direction: row;
	justify-content: space-between;
	align-items: center;
	margin-bottom: 1rem;
}

.wizard-step {
	display: flex;
	flex-direction: row;
	align-items: center;
	flex: 1;
}

.wizard-step:first-child {
	flex: none;
}

.wizard-step h2 {
	white-space: nowrap;
}

.wizard-step-separator {
	flex: 1;
	height: 2px;
	margin: 0 0.5rem;
}

.wizard-step-separator-completed {
	background-color: var(--color-primary);
}

.wizard-step-separator-open {
	background-color: var(--color-accent);
}
</style>
