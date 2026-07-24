<script setup lang="ts">
import { ref } from "vue";
import { Input } from "@/components/ui/input";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Textarea } from "@/components/ui/textarea";

import { type model } from "../..";
import { Separator } from "@/components/ui/separator";

const props = defineProps<{
	ticketNames: model.TicketTypeDetails[];
	onStep: (ticketName: model.TicketTypeDetails) => void;
}>();

const ticketName = ref<string>("");
const ticketDescription = ref<string>("");

function addTicket() {
	const name = ticketName.value.trim();

	if (name === "") {
		return;
	}

	if (props.ticketNames.some((ticket) => ticket.name === name)) {
		alert("Ein Ticket mit diesem Namen existiert bereits.");
		return;
	}

	props.onStep({
		name: name,
		description: ticketDescription.value.trim(),
	});
	ticketName.value = "";
}
</script>

<template>
	<div class="grid max-w-5xl gap-6 pl-6 md:grid-cols-2">
		<Card>
			<CardHeader>
				<CardTitle>Ticket-Typ hinzufügen</CardTitle>
			</CardHeader>
			<CardContent class="flex flex-col gap-4">
				<Input v-model="ticketName" label="Name" />
				<Textarea v-model="ticketDescription" label="Beschreibung" />
				<Button @click="addTicket">Hinzufügen</Button>
			</CardContent>
		</Card>

		<Card>
			<CardHeader>
				<CardTitle>Bestehende Ticket-Typen</CardTitle>
			</CardHeader>
			<CardContent class="flex flex-col">
				<p v-if="ticketNames.length === 0" class="text-muted-foreground text-sm">
					Noch keine Ticket-Typen vorhanden.
				</p>
				<template v-for="(ticket, i) in ticketNames" :key="ticket.name">
					<Separator v-if="i > 0" />
					<div class="flex flex-col gap-1 py-4 first:pt-0 last:pb-0">
						<p class="font-medium">{{ ticket.name }}</p>
						<p class="text-muted-foreground text-sm">
							{{ ticket.description || "Keine Beschreibung" }}
						</p>
					</div>
				</template>
			</CardContent>
		</Card>
	</div>
</template>
