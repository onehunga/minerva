<script setup lang="ts">
import { onMounted, ref, watch } from "vue";
import { useTicketRepository } from "../composables/useTicketRepository";
import type { TicketComment } from "../ticket.model";

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

function formatDate(value: string | null): string {
	if (value == null) {
		return "-";
	}

	return new Intl.DateTimeFormat("de-DE", {
		dateStyle: "medium",
		timeStyle: "short",
	}).format(new Date(value));
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
	<section class="ticket-comments" aria-labelledby="ticket-comments-heading">
		<h4 id="ticket-comments-heading">Kommentare</h4>

		<p v-if="isLoading">Kommentare werden geladen...</p>
		<p v-else-if="errorMessage" role="alert">{{ errorMessage }}</p>
		<p v-else-if="comments.length === 0">Keine Kommentare vorhanden.</p>
		<ul v-else class="ticket-comments__list">
			<li v-for="comment in comments" :key="comment.id" class="ticket-comments__item">
				<p>{{ comment.content }}</p>
				<small>{{ comment.authorUsername }} - {{ formatDate(comment.createdAt) }}</small>
			</li>
		</ul>

		<form v-if="canCreateComment" class="ticket-comments__form" @submit.prevent="createComment">
			<textarea
				v-model="content"
				aria-label="Kommentar"
				placeholder="Kommentar schreiben"
				:disabled="isCreating"
			></textarea>
			<button type="submit" :disabled="isCreating || !content.trim()">
				{{ isCreating ? "Kommentar wird gespeichert..." : "Kommentieren" }}
			</button>
			<p v-if="createErrorMessage" class="form-message" role="alert">
				{{ createErrorMessage }}
			</p>
		</form>
	</section>
</template>

<style scoped>
.ticket-comments {
	display: flex;
	flex-direction: column;
	gap: 0.75rem;
}

.ticket-comments h4,
.ticket-comments p {
	margin: 0;
}

.ticket-comments__list {
	display: flex;
	flex-direction: column;
	gap: 0.5rem;
	margin: 0;
	padding: 0;
	list-style: none;
}

.ticket-comments__item {
	display: flex;
	flex-direction: column;
	gap: 0.3rem;
	padding: 0.6rem 0.8rem;
	border: 1px solid currentColor;
}

.ticket-comments__form {
	display: flex;
	flex-direction: column;
	gap: 0.75rem;
	max-width: 32rem;
}

.ticket-comments__form textarea {
	min-height: 5rem;
	padding: 0.45rem 0.55rem;
	border: 1px solid currentColor;
	background: Canvas;
	color: CanvasText;
	resize: vertical;
}

.ticket-comments__form button {
	align-self: flex-start;
}

.form-message {
	margin: 0;
}
</style>
