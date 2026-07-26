<script setup lang="ts">
import { computed, ref, watch } from "vue";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Separator } from "@/components/ui/separator";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { Textarea } from "@/components/ui/textarea";
import type { TicketStatusCategory, TicketWorkflow } from "@/feature/ticket";
import {
	getWorkflowTransitionTargets,
	groupWorkflowTransitions,
	isWorkflowConnectionAvailable,
	isWorkflowStateReferenced,
	type WorkflowTransition,
} from "../../create-project-workflow";

const props = defineProps<{
	workflow: TicketWorkflow;
	ticketNames: string[];
}>();
const emit = defineEmits<{ update: [workflow: TicketWorkflow] }>();

const categoryLabels: Record<TicketStatusCategory, string> = {
	OPEN: "Offen",
	IN_PROGRESS: "In Bearbeitung",
	COMPLETED: "Abgeschlossen",
};
const categories = Object.keys(categoryLabels) as TicketStatusCategory[];
const wildcardState = "__all_states__";

const detailName = ref("");
const detailDescription = ref("");
const newStateName = ref("");
const newStateCategory = ref<TicketStatusCategory>("OPEN");
const newTransitionName = ref("");
const newTransitionFrom = ref(wildcardState);
const newTransitionTo = ref("");
const newChildName = ref("");
const errorMessage = ref("");

const possibleChildren = computed(() =>
	props.ticketNames.filter((name) => !props.workflow.children.includes(name)),
);
const stateNames = computed(() => props.workflow.states.map((state) => state.name));
const transitionGroups = computed(() => groupWorkflowTransitions(props.workflow));
const possibleNewTransitionSources = computed(() =>
	[wildcardState, ...stateNames.value].filter(
		(source) => possibleTransitionTargets(sourceToState(source)).length > 0,
	),
);
const possibleNewTransitionTargets = computed(() =>
	possibleTransitionTargets(sourceToState(newTransitionFrom.value)),
);

watch(
	() => [props.workflow.name, props.workflow.description],
	([name, description]) => {
		detailName.value = name ?? "";
		detailDescription.value = description ?? "";
	},
	{ immediate: true },
);

watch(possibleChildren, (children) => {
	if (!children.includes(newChildName.value)) {
		newChildName.value = "";
	}
});

watch(
	possibleNewTransitionSources,
	(sources) => {
		if (!sources.includes(newTransitionFrom.value)) {
			newTransitionFrom.value = sources[0] ?? wildcardState;
		}
	},
	{ immediate: true },
);

watch(
	possibleNewTransitionTargets,
	(targets) => {
		if (!targets.includes(newTransitionTo.value)) {
			newTransitionTo.value = "";
		}
	},
	{ immediate: true },
);

function sourceToState(source: string): string | null {
	return source === wildcardState ? null : source;
}

function stateToSource(state: string | null): string {
	return state ?? wildcardState;
}

function possibleTransitionSources(current: WorkflowTransition): string[] {
	return [wildcardState, ...stateNames.value].filter((source) =>
		isWorkflowConnectionAvailable(props.workflow, sourceToState(source), current.to, current),
	);
}

function possibleTransitionTargets(from: string | null, current?: WorkflowTransition): string[] {
	return getWorkflowTransitionTargets(props.workflow, from, current);
}

function transitionKey(transition: WorkflowTransition): string {
	return JSON.stringify([transition.from, transition.to]);
}

function update(changes: Partial<TicketWorkflow>) {
	errorMessage.value = "";
	emit("update", { ...props.workflow, ...changes });
}

function saveDetails() {
	const name = detailName.value.trim();
	if (name === "") {
		errorMessage.value = "Der Ticket-Name darf nicht leer sein.";
		return;
	}
	if (name !== props.workflow.name && props.ticketNames.includes(name)) {
		errorMessage.value = "Ein Ticket-Typ mit diesem Namen existiert bereits.";
		return;
	}

	update({ name, description: detailDescription.value.trim() });
}

function addState() {
	const name = newStateName.value.trim();
	if (name === "") {
		errorMessage.value = "Bitte geben Sie einen Zustandsnamen an.";
		return;
	}
	if (props.workflow.states.some((state) => state.name === name)) {
		errorMessage.value = "Ein Zustand mit diesem Namen existiert bereits.";
		return;
	}

	update({
		states: [...props.workflow.states, { name, category: newStateCategory.value }],
	});
	newStateName.value = "";
}

function updateStateCategory(stateName: string, category: TicketStatusCategory) {
	update({
		states: props.workflow.states.map((state) =>
			state.name === stateName ? { ...state, category } : state,
		),
	});
}

function removeState(stateName: string) {
	if (isWorkflowStateReferenced(props.workflow, stateName)) {
		errorMessage.value = "Dieser Zustand wird noch von einem Übergang verwendet.";
		return;
	}

	update({ states: props.workflow.states.filter((state) => state.name !== stateName) });
}

function addTransition() {
	const name = newTransitionName.value.trim();
	const from = sourceToState(newTransitionFrom.value);
	const to = newTransitionTo.value;

	if (name === "" || to === "") {
		errorMessage.value = "Bitte geben Sie einen Namen und einen Zielzustand an.";
		return;
	}
	if ((from != null && !stateNames.value.includes(from)) || !stateNames.value.includes(to)) {
		errorMessage.value = "Bitte wählen Sie gültige Zustände aus.";
		return;
	}
	if (!isWorkflowConnectionAvailable(props.workflow, from, to)) {
		errorMessage.value = "Dieser Übergang ist bereits definiert.";
		return;
	}

	update({ transitions: [...props.workflow.transitions, { name, from, to }] });
	newTransitionName.value = "";
	newTransitionFrom.value = wildcardState;
	newTransitionTo.value = "";
}

function updateTransition(
	transition: WorkflowTransition,
	changes: { from?: string | null; to?: string },
) {
	const from = "from" in changes ? (changes.from ?? null) : transition.from;
	const to = changes.to ?? transition.to;
	if (!isWorkflowConnectionAvailable(props.workflow, from, to, transition)) {
		errorMessage.value = "Dieser Übergang ist bereits definiert.";
		return;
	}

	update({
		transitions: props.workflow.transitions.map((item) =>
			item === transition ? { ...item, from, to } : item,
		),
	});
}

function removeTransition(transition: WorkflowTransition) {
	update({ transitions: props.workflow.transitions.filter((item) => item !== transition) });
}

function addChild() {
	if (newChildName.value === "") {
		return;
	}

	update({ children: [...props.workflow.children, newChildName.value] });
	newChildName.value = "";
}
</script>

<template>
	<Card>
		<CardHeader>
			<CardTitle>{{ workflow.name }} konfigurieren</CardTitle>
		</CardHeader>
		<CardContent>
			<Tabs default-value="details">
				<div class="overflow-x-auto pb-1">
					<TabsList class="min-w-max">
						<TabsTrigger value="details">Details</TabsTrigger>
						<TabsTrigger value="states">Zustände</TabsTrigger>
						<TabsTrigger value="transitions">Übergänge</TabsTrigger>
						<TabsTrigger value="children">Untertickets</TabsTrigger>
					</TabsList>
				</div>

				<p v-if="errorMessage" role="alert" class="text-destructive mt-4 text-sm">
					{{ errorMessage }}
				</p>

				<TabsContent value="details" class="mt-5 max-w-2xl">
					<form class="flex flex-col gap-4" @submit.prevent="saveDetails">
						<div class="flex flex-col gap-2">
							<Label for="workflow-name">Name</Label>
							<Input id="workflow-name" v-model="detailName" required />
						</div>
						<div class="flex flex-col gap-2">
							<Label for="workflow-description">Beschreibung</Label>
							<Textarea id="workflow-description" v-model="detailDescription" />
						</div>
						<Button type="submit" class="self-start">Details übernehmen</Button>
					</form>
				</TabsContent>

				<TabsContent value="states" class="mt-5">
					<div class="flex flex-col gap-4">
						<p v-if="workflow.states.length === 0" class="text-muted-foreground">
							Noch keine Zustände vorhanden. Mindestens ein Zustand ist erforderlich.
						</p>
						<template v-for="(state, index) in workflow.states" :key="state.name">
							<Separator v-if="index > 0" />
							<div class="flex flex-col gap-3 sm:flex-row sm:items-center">
								<span class="min-w-0 flex-1 font-medium">{{ state.name }}</span>
								<select
									:value="state.category"
									class="border-input bg-background h-8 rounded-md border px-3 text-sm"
									@change="
										updateStateCategory(
											state.name,
											($event.target as HTMLSelectElement)
												.value as TicketStatusCategory,
										)
									"
								>
									<option
										v-for="category in categories"
										:key="category"
										:value="category"
									>
										{{ categoryLabels[category] }}
									</option>
								</select>
								<Button
									type="button"
									variant="destructive"
									size="sm"
									@click="removeState(state.name)"
								>
									Löschen
								</Button>
							</div>
						</template>

						<Separator />
						<form
							class="flex flex-col gap-3 sm:flex-row sm:items-end"
							@submit.prevent="addState"
						>
							<div class="flex min-w-56 flex-1 flex-col gap-2">
								<Label for="new-state-name">Neuer Zustand</Label>
								<Input id="new-state-name" v-model="newStateName" />
							</div>
							<div class="flex flex-col gap-2">
								<Label for="new-state-category">Kategorie</Label>
								<select
									id="new-state-category"
									v-model="newStateCategory"
									class="border-input bg-background h-8 rounded-md border px-3 text-sm"
								>
									<option
										v-for="category in categories"
										:key="category"
										:value="category"
									>
										{{ categoryLabels[category] }}
									</option>
								</select>
							</div>
							<Button type="submit">Zustand hinzufügen</Button>
						</form>
					</div>
				</TabsContent>

				<TabsContent value="transitions" class="mt-5">
					<div class="flex flex-col gap-4">
						<p v-if="workflow.states.length === 0" class="text-muted-foreground">
							Legen Sie zuerst mindestens einen Zustand an.
						</p>
						<p
							v-else-if="workflow.transitions.length === 0"
							class="text-muted-foreground"
						>
							Noch keine Übergänge vorhanden.
						</p>
						<template v-for="(group, groupIndex) in transitionGroups" :key="group.name">
							<Separator v-if="groupIndex > 0" />
							<div
								class="border-border rounded-lg border p-4"
								:data-transition-group="group.name"
							>
								<div class="mb-4 flex items-center justify-between gap-3">
									<h3 class="font-semibold">{{ group.name }}</h3>
									<span class="text-muted-foreground text-xs">
										{{ group.connections.length }}
										{{
											group.connections.length === 1
												? "Verbindung"
												: "Verbindungen"
										}}
									</span>
								</div>
								<div class="flex flex-col gap-3">
									<div
										v-for="transition in group.connections"
										:key="transitionKey(transition)"
										:data-transition="transitionKey(transition)"
										class="grid gap-3 lg:grid-cols-[minmax(10rem,1fr)_auto_minmax(10rem,1fr)_auto] lg:items-center"
									>
										<select
											:value="stateToSource(transition.from)"
											class="border-input bg-background h-8 min-w-0 rounded-md border px-3 text-sm"
											@change="
												updateTransition(transition, {
													from: sourceToState(
														($event.target as HTMLSelectElement).value,
													),
												})
											"
										>
											<option
												v-for="source in possibleTransitionSources(
													transition,
												)"
												:key="source"
												:value="source"
											>
												{{
													source === wildcardState
														? "Alle Zustände"
														: source
												}}
											</option>
										</select>
										<span class="hidden text-center lg:block">→</span>
										<select
											:value="transition.to"
											class="border-input bg-background h-8 min-w-0 rounded-md border px-3 text-sm"
											@change="
												updateTransition(transition, {
													to: ($event.target as HTMLSelectElement).value,
												})
											"
										>
											<option
												v-for="state in possibleTransitionTargets(
													transition.from,
													transition,
												)"
												:key="state"
												:value="state"
											>
												{{ state }}
											</option>
										</select>
										<Button
											type="button"
											variant="destructive"
											size="sm"
											@click="removeTransition(transition)"
										>
											Löschen
										</Button>
									</div>
								</div>
							</div>
						</template>

						<Separator />
						<form
							class="grid gap-3 lg:grid-cols-[minmax(10rem,1fr)_minmax(10rem,1fr)_minmax(10rem,1fr)_auto] lg:items-end"
							@submit.prevent="addTransition"
						>
							<div class="flex flex-col gap-2">
								<Label for="new-transition-name">Name / Gruppe</Label>
								<Input
									id="new-transition-name"
									v-model="newTransitionName"
									list="transition-group-names"
								/>
								<datalist id="transition-group-names">
									<option
										v-for="group in transitionGroups"
										:key="group.name"
										:value="group.name"
									/>
								</datalist>
							</div>
							<div class="flex flex-col gap-2">
								<Label for="new-transition-from">Von</Label>
								<select
									id="new-transition-from"
									v-model="newTransitionFrom"
									class="border-input bg-background h-8 min-w-0 rounded-md border px-3 text-sm"
								>
									<option
										v-for="source in possibleNewTransitionSources"
										:key="source"
										:value="source"
									>
										{{ source === wildcardState ? "Alle Zustände" : source }}
									</option>
								</select>
							</div>
							<div class="flex flex-col gap-2">
								<Label for="new-transition-to">Nach</Label>
								<select
									id="new-transition-to"
									v-model="newTransitionTo"
									class="border-input bg-background h-8 min-w-0 rounded-md border px-3 text-sm"
								>
									<option value="">Zustand wählen</option>
									<option
										v-for="state in possibleNewTransitionTargets"
										:key="state"
										:value="state"
									>
										{{ state }}
									</option>
								</select>
							</div>
							<Button
								type="submit"
								:disabled="possibleNewTransitionSources.length === 0"
							>
								Übergang hinzufügen
							</Button>
						</form>
					</div>
				</TabsContent>

				<TabsContent value="children" class="mt-5">
					<div class="flex max-w-3xl flex-col gap-4">
						<p v-if="workflow.children.length === 0" class="text-muted-foreground">
							Noch keine Unterticket-Typen erlaubt.
						</p>
						<template v-for="(child, index) in workflow.children" :key="child">
							<Separator v-if="index > 0" />
							<div class="flex items-center justify-between gap-3">
								<span class="font-medium">{{ child }}</span>
								<Button
									type="button"
									variant="destructive"
									size="sm"
									@click="
										update({
											children: workflow.children.filter(
												(name) => name !== child,
											),
										})
									"
								>
									Entfernen
								</Button>
							</div>
						</template>

						<Separator />
						<form
							class="flex flex-col gap-3 sm:flex-row sm:items-end"
							@submit.prevent="addChild"
						>
							<div class="flex min-w-0 flex-1 flex-col gap-2">
								<Label for="new-child">Unterticket-Typ</Label>
								<select
									id="new-child"
									v-model="newChildName"
									class="border-input bg-background h-8 rounded-md border px-3 text-sm"
								>
									<option value="">Ticket-Typ wählen</option>
									<option
										v-for="name in possibleChildren"
										:key="name"
										:value="name"
									>
										{{ name }}
									</option>
								</select>
							</div>
							<Button type="submit" :disabled="newChildName === ''"
								>Unterticket erlauben</Button
							>
						</form>
					</div>
				</TabsContent>
			</Tabs>
		</CardContent>
	</Card>
</template>
