import { ref, toValue, watch, type MaybeRefOrGetter } from "vue";
import type { TicketComment } from "../ticket.model";
import { useTicketRepository } from "./useTicketRepository";

// eslint-disable-next-line @typescript-eslint/explicit-module-boundary-types
export function useTicketComments(
	projectId: MaybeRefOrGetter<number>,
	ticketId: MaybeRefOrGetter<number>,
) {
	const repository = useTicketRepository();
	const comments = ref<TicketComment[]>([]);
	const isLoading = ref(false);
	const isCreating = ref(false);
	const hasLoadError = ref(false);
	const hasCreateError = ref(false);

	async function load([currentProjectId, currentTicketId]: readonly [
		number,
		number,
	]): Promise<void> {
		comments.value = [];
		isLoading.value = true;
		hasLoadError.value = false;
		hasCreateError.value = false;

		try {
			comments.value = await repository.getTicketComments(currentProjectId, currentTicketId);
		} catch {
			hasLoadError.value = true;
		} finally {
			isLoading.value = false;
		}
	}

	async function createComment(content: string): Promise<boolean> {
		if (!content.trim() || isCreating.value) {
			return false;
		}

		isCreating.value = true;
		hasCreateError.value = false;

		try {
			const comment = await repository.createTicketComment(
				toValue(projectId),
				toValue(ticketId),
				{ content },
			);
			comments.value = [
				...comments.value,
				{
					...comment,
					createdAt: comment.createdAt ?? new Date().toISOString(),
				},
			];
			return true;
		} catch {
			hasCreateError.value = true;
			return false;
		} finally {
			isCreating.value = false;
		}
	}

	watch(() => [toValue(projectId), toValue(ticketId)] as const, load, { immediate: true });

	return {
		comments,
		isLoading,
		isCreating,
		hasLoadError,
		hasCreateError,
		createComment,
	};
}
