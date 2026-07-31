<script setup lang="ts">
import { computed, ref, watch } from "vue";
import { useTicketComments } from "../composables/useTicketComments";
import { Alert, AlertDescription } from "@/components/ui/alert";
import { Button } from "@/components/ui/button";
import { Textarea } from "@/components/ui/textarea";
import { formatDate } from "@/lib/date";
import { useUserStore } from "@/feature/user";

const props = withDefaults(
	defineProps<{
		projectId: number;
		ticketId: number;
		canCreateComment: boolean;
		disabled?: boolean;
	}>(),
	{ disabled: false },
);

const content = ref("");
const userStore = useUserStore();
const { comments, isLoading, isCreating, hasLoadError, hasCreateError, createComment } =
	useTicketComments(
		() => props.projectId,
		() => props.ticketId,
	);

// API liefert chronologisch; neu erstellte Kommentare werden angehängt → umkehren genügt.
const newestFirstComments = computed(() => [...comments.value].reverse());

async function submitComment(): Promise<void> {
	if (props.disabled) {
		return;
	}

	if (await createComment(content.value)) {
		content.value = "";
	}
}

watch(
	() => [props.projectId, props.ticketId],
	() => {
		content.value = "";
	},
);
</script>

<template>
	<section class="flex flex-col gap-3" aria-labelledby="ticket-comments-heading">
		<h4 id="ticket-comments-heading" class="m-0">Kommentare</h4>

		<p v-if="isLoading" class="m-0">Kommentare werden geladen...</p>
		<Alert v-else-if="hasLoadError" variant="destructive">
			<AlertDescription>Kommentare konnten nicht geladen werden.</AlertDescription>
		</Alert>
		<p v-else-if="comments.length === 0" class="m-0">Keine Kommentare vorhanden.</p>
		<ul v-else class="m-0 flex list-none flex-col gap-2 p-0">
			<li
				v-for="comment in newestFirstComments"
				:key="comment.id"
				class="flex flex-col gap-1 rounded-md border px-3 py-2"
				:class="{ 'bg-muted/50': comment.authorId === userStore.userDetails?.id }"
				:data-own-comment="comment.authorId === userStore.userDetails?.id || undefined"
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
			@submit.prevent="submitComment"
		>
			<Textarea
				v-model="content"
				aria-label="Kommentar"
				placeholder="Kommentar schreiben"
				class="min-h-20 resize-y"
				:disabled="isCreating || disabled"
			/>
			<Button
				type="submit"
				class="self-start"
				:disabled="isCreating || disabled || !content.trim()"
			>
				{{ isCreating ? "Kommentar wird gespeichert..." : "Kommentieren" }}
			</Button>
			<Alert v-if="hasCreateError" variant="destructive">
				<AlertDescription>Kommentar konnte nicht gespeichert werden.</AlertDescription>
			</Alert>
		</form>
	</section>
</template>
