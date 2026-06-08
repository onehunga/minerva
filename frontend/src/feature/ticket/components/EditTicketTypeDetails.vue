<script setup lang="ts">
import { ref } from "vue";
import type { TicketDetails } from "../ticket.model";

type Props = TicketDetails & { submitName?: string };

const props = defineProps<Props>();

const name = ref(props.name);
const description = ref(props.description);

defineEmits<{
	(event: "submit", details: TicketDetails): void;
}>();
</script>

<template>
	<div class="edit-ticket-type-details">
		<form @submit.prevent="$emit('submit', { name, description })">
			<div class="form-field">
				<label for="ticket-name">Name:</label>
				<input id="ticket-name" v-model="name" />
			</div>
			<div class="form-field">
				<label for="ticket-description">Beschreibung:</label>
				<textarea id="ticket-description" v-model="description"></textarea>
			</div>
			<button type="submit">{{ submitName || "Änderung Übernehmen" }}</button>
		</form>
	</div>
</template>
