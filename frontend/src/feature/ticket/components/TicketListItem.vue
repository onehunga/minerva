<script setup lang="ts">
import Separator from "@/components/ui/separator/Separator.vue";
import type { Ticket } from "../ticket.model";
import { ref } from "vue";
import { Collapsible, CollapsibleTrigger, CollapsibleContent } from "@/components/ui/collapsible";
import { HugeiconsIcon } from "@hugeicons/vue";
import { ChevronUp, ChevronDown } from "@hugeicons/core-free-icons";

defineProps<{
	ticket: Ticket;
	selectedTicketId: number | null;
}>();

const emit = defineEmits<{
	selectTicket: [ticketId: number];
}>();

const showChildren = ref<boolean>(false);
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
			:aria-current="selectedTicketId === ticket.id ? 'true' : undefined"
			@click="emit('selectTicket', ticket.id)"
		>
			<span class="min-w-0 truncate">{{ ticket.name }}</span>
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
								<span class="min-w-0 truncate">{{ child.name }}</span>
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
