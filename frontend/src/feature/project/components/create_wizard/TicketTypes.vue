<script setup lang="ts">
import { ref } from "vue";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Separator } from "@/components/ui/separator";
import { Textarea } from "@/components/ui/textarea";
import type { TicketWorkflow } from "@/feature/ticket";

const props = defineProps<{ tickets: TicketWorkflow[] }>();
const emit = defineEmits<{
	add: [ticket: TicketWorkflow];
	open: [ticketName: string];
	remove: [ticketName: string];
}>();

const ticketName = ref("");
const ticketDescription = ref("");
const errorMessage = ref("");

function addTicket() {
	const name = ticketName.value.trim();
	errorMessage.value = "";

	if (name === "") {
		errorMessage.value = "Bitte geben Sie einen Namen an.";
		return;
	}
	if (props.tickets.some((ticket) => ticket.name === name)) {
		errorMessage.value = "Ein Ticket-Typ mit diesem Namen existiert bereits.";
		return;
	}

	emit("add", {
		name,
		description: ticketDescription.value.trim(),
		states: [],
		transitions: [],
		children: [],
	});
	ticketName.value = "";
	ticketDescription.value = "";
}

function removeTicket(ticketName: string) {
	if (window.confirm(`Ticket-Typ „${ticketName}“ wirklich löschen?`)) {
		emit("remove", ticketName);
	}
}
</script>

<template>
	<div class="grid gap-6 xl:grid-cols-[minmax(18rem,24rem)_minmax(0,1fr)]">
		<Card>
			<CardHeader>
				<CardTitle>Ticket-Typ hinzufügen</CardTitle>
			</CardHeader>
			<CardContent>
				<form class="flex flex-col gap-4" @submit.prevent="addTicket">
					<div class="flex flex-col gap-2">
						<Label for="ticket-name">Name</Label>
						<Input id="ticket-name" v-model="ticketName" />
					</div>
					<div class="flex flex-col gap-2">
						<Label for="ticket-description">Beschreibung</Label>
						<Textarea id="ticket-description" v-model="ticketDescription" />
					</div>
					<p v-if="errorMessage" role="alert" class="text-destructive text-sm">
						{{ errorMessage }}
					</p>
					<Button type="submit">Hinzufügen</Button>
				</form>
			</CardContent>
		</Card>

		<Card>
			<CardHeader>
				<CardTitle>Ticket-Typen</CardTitle>
			</CardHeader>
			<CardContent>
				<p v-if="tickets.length === 0" class="text-muted-foreground text-sm">
					Noch keine Ticket-Typen vorhanden.
				</p>
				<template v-for="(ticket, index) in tickets" :key="ticket.name">
					<Separator v-if="index > 0" />
					<div
						class="flex flex-col gap-3 py-4 first:pt-0 last:pb-0 sm:flex-row sm:items-center"
					>
						<div class="min-w-0 flex-1">
							<p class="truncate font-medium">{{ ticket.name }}</p>
							<p class="text-muted-foreground truncate text-sm">
								{{ ticket.description || "Keine Beschreibung" }}
							</p>
							<p class="text-muted-foreground mt-1 text-xs">
								{{ ticket.states.length }} Zustände ·
								{{ ticket.transitions.length }} Übergänge ·
								{{ ticket.children.length }} Untertickets
							</p>
						</div>
						<div class="flex gap-2">
							<Button type="button" size="sm" @click="$emit('open', ticket.name)">
								Bearbeiten
							</Button>
							<Button
								type="button"
								size="sm"
								variant="outline"
								@click="removeTicket(ticket.name)"
							>
								Löschen
							</Button>
						</div>
					</div>
				</template>
			</CardContent>
		</Card>
	</div>
</template>
