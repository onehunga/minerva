<script setup lang="ts">
import { computed, onMounted, ref, watch } from "vue";
import { useTicketRepository } from "../composables/useTicketRepository";
import type { TicketComment } from "../ticket.model";
import { Alert, AlertDescription } from "@/components/ui/alert";
import { Button } from "@/components/ui/button";
import { Textarea } from "@/components/ui/textarea";
import { formatDate } from "@/lib/date";

const props = defineProps<{
	projectId: number;
	ticketId: number;
	canCreateComment: boolean;
}>();

const ticketRepository = useTicketRepository();

const comments = ref<TicketComment[]>([]);
const content = ref("");
const isLoading = ref(false);
const isCreating = ref(false);
const errorMessage = ref("");
const createErrorMessage = ref("");

// API liefert chronologisch; neu erstellte Kommentare werden angehängt → umkehren genügt.
const newestFirstComments = computed(() => [...comments.value].reverse());

async function loadComments(): Promise<void> {
	isLoading.value = true;
	errorMessage.value = "";

	try {
		comments.value = await ticketRepository.getTicketComments(props.projectId, props.ticketId);
	} catch {
		comments.value = [];
		errorMessage.value = "Kommentare konnten nicht geladen werden.";
	} finally {
		isLoading.value = false;
	}
}

async function createComment(): Promise<void> {
	if (!content.value.trim() || isCreating.value) {
		return;
	}

	isCreating.value = true;
	createErrorMessage.value = "";

	try {
		const comment = await ticketRepository.createTicketComment(
			props.projectId,
			props.ticketId,
			{
				content: content.value,
			},
		);

		comments.value = [
			...comments.value,
			{
				...comment,
				createdAt: comment.createdAt ?? new Date().toISOString(),
			},
		];
		content.value = "";
	} catch {
		createErrorMessage.value = "Kommentar konnte nicht gespeichert werden.";
	} finally {
		isCreating.value = false;
	}
}

onMounted(() => {
	loadComments();
});

watch(
	() => [props.projectId, props.ticketId],
	() => {
		content.value = "";
		createErrorMessage.value = "";
		loadComments();
	},
);
</script>

<template>
	<section class="flex flex-col gap-3" aria-labelledby="ticket-comments-heading">
		<h4 id="ticket-comments-heading" class="m-0">Kommentare</h4>

		<p v-if="isLoading" class="m-0">Kommentare werden geladen...</p>
		<Alert v-else-if="errorMessage" variant="destructive">
			<AlertDescription>{{ errorMessage }}</AlertDescription>
		</Alert>
		<p v-else-if="comments.length === 0" class="m-0">Keine Kommentare vorhanden.</p>
		<ul v-else class="m-0 flex list-none flex-col gap-2 p-0">
			<li
				v-for="comment in newestFirstComments"
				:key="comment.id"
				class="flex flex-col gap-1 rounded-md border px-3 py-2"
			>
				<p class="m-0">{{ comment.content }}</p>
				<small class="text-muted-foreground">
					{{ comment.authorDeleted ? "Gelöschter Nutzer" : comment.authorUsername }} -
					{{ formatDate(comment.createdAt) }}
				</small>
			</li>
		</ul>

		<form
			v-if="canCreateComment"
			class="flex max-w-lg flex-col gap-3"
			@submit.prevent="createComment"
		>
			<Textarea
				v-model="content"
				aria-label="Kommentar"
				placeholder="Kommentar schreiben"
				class="min-h-20 resize-y"
				:disabled="isCreating"
			/>
			<Button type="submit" class="self-start" :disabled="isCreating || !content.trim()">
				{{ isCreating ? "Kommentar wird gespeichert..." : "Kommentieren" }}
			</Button>
			<Alert v-if="createErrorMessage" variant="destructive">
				<AlertDescription>{{ createErrorMessage }}</AlertDescription>
			</Alert>
		</form>
	</section>
</template>
