<script setup lang="ts">
import Separator from "@/components/ui/separator/Separator.vue";
import type { Ticket, TicketChild, TicketType, WorkflowState } from "../ticket.model";
import { formatTicketPriority, ticketPriorityIndicatorClass } from "../priority-labels";
import { ref } from "vue";
import { Collapsible, CollapsibleTrigger, CollapsibleContent } from "@/components/ui/collapsible";
import { HugeiconsIcon } from "@hugeicons/vue";
import { ChevronUp, ChevronDown } from "@hugeicons/core-free-icons";

const props = defineProps<{
	ticket: Ticket;
	allTickets: Ticket[];
	ticketTypes: TicketType[];
	selectedTicketId: number | null;
}>();

const emit = defineEmits<{
	selectTicket: [ticketId: number];
}>();

const showChildren = ref<boolean>(false);

function fullChild(child: TicketChild): Ticket | undefined {
	return props.allTickets.find((ticket) => ticket.id === child.id);
}

function ticketStatus(ticket: Ticket): WorkflowState | undefined {
	return props.ticketTypes
		.find((ticketType) => ticketType.id === ticket.ticketTypeId)
		?.states.find((state) => state.id === ticket.statusId);
}

function statusClass(status: WorkflowState | undefined): string {
	switch (status?.category) {
		case "OPEN":
			return "border-sky-500/30 bg-sky-500/10 text-sky-700 dark:text-sky-300";
		case "IN_PROGRESS":
			return "border-amber-500/30 bg-amber-500/10 text-amber-700 dark:text-amber-300";
		case "COMPLETED":
			return "border-emerald-500/30 bg-emerald-500/10 text-emerald-700 dark:text-emerald-300";
		default:
			return "bg-muted text-muted-foreground";
	}
}
</script>

<template>
	<div
		class="overflow-hidden rounded-lg border bg-card transition-colors"
		:class="{ 'border-primary bg-accent/30': selectedTicketId === ticket.id }"
	>
		<button
			type="button"
			class="flex w-full items-center justify-between gap-3 px-3 py-2.5 text-left outline-none transition-colors hover:bg-muted/60 focus-visible:ring-3 focus-visible:ring-ring/50"
			:class="{ 'font-semibold': selectedTicketId === ticket.id }"
			:aria-label="ticket.name"
			:aria-current="selectedTicketId === ticket.id ? 'true' : undefined"
			@click="emit('selectTicket', ticket.id)"
		>
			<span class="min-w-0 flex-1">
				<span class="block truncate">{{ ticket.name }}</span>
				<span class="mt-1 flex flex-wrap items-center gap-2 text-xs font-normal">
					<span
						class="rounded-full border px-2 py-0.5"
						:class="statusClass(ticketStatus(ticket))"
					>
						{{ ticketStatus(ticket)?.name ?? "Unbekannt" }}
					</span>
					<span class="flex items-center gap-1 text-muted-foreground">
						<span
							class="size-2 rounded-full"
							:class="ticketPriorityIndicatorClass(ticket.priority)"
						></span>
						{{ formatTicketPriority(ticket.priority) }}
					</span>
				</span>
			</span>
			<small class="shrink-0 text-muted-foreground">#{{ ticket.id }}</small>
		</button>

		<template v-if="ticket.children.length > 0">
			<Separator />
			<Collapsible v-model:open="showChildren">
				<CollapsibleTrigger
					type="button"
					class="flex w-full items-center justify-between gap-2 px-3 py-2 text-xs font-medium text-muted-foreground outline-none transition-colors hover:bg-muted/40 hover:text-foreground focus-visible:ring-3 focus-visible:ring-ring/50"
				>
					<span>Untertickets anzeigen ({{ ticket.children.length }})</span>
					<HugeiconsIcon
						:icon="showChildren ? ChevronUp : ChevronDown"
						class="size-4 shrink-0"
					/>
				</CollapsibleTrigger>
				<CollapsibleContent class="bg-muted/20 px-3 pb-3">
					<ul class="m-0 flex list-none flex-col gap-1 border-l p-0 pl-3">
						<li v-for="child in ticket.children" :key="child.id">
							<button
								type="button"
								class="flex w-full items-center justify-between gap-3 rounded-md border bg-card px-3 py-2 text-left text-sm outline-none transition-colors hover:bg-muted focus-visible:ring-3 focus-visible:ring-ring/50"
								:class="{
									'border-primary bg-accent font-semibold':
										selectedTicketId === child.id,
								}"
								:aria-label="`Ticket ${child.name} auswählen`"
								:aria-current="selectedTicketId === child.id ? 'true' : undefined"
								@click="emit('selectTicket', child.id)"
							>
								<span class="min-w-0 flex-1">
									<span class="block truncate">{{ child.name }}</span>
									<span
										v-if="fullChild(child)"
										class="mt-1 flex items-center gap-2 text-xs font-normal text-muted-foreground"
									>
										<span
											class="rounded-full border px-2 py-0.5"
											:class="statusClass(ticketStatus(fullChild(child)!))"
										>
											{{
												ticketStatus(fullChild(child)!)?.name ?? "Unbekannt"
											}}
										</span>
										<span class="flex items-center gap-1">
											<span
												class="size-2 rounded-full"
												:class="
													ticketPriorityIndicatorClass(
														fullChild(child)!.priority,
													)
												"
											></span>
											{{ formatTicketPriority(fullChild(child)!.priority) }}
										</span>
									</span>
								</span>
								<small class="shrink-0 text-muted-foreground"
									>#{{ child.id }}</small
								>
							</button>
						</li>
					</ul>
				</CollapsibleContent>
			</Collapsible>
		</template>
	</div>
</template>
