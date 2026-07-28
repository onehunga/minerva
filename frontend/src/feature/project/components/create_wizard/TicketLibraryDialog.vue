<script setup lang="ts">
import { computed, ref } from "vue";
import { Button } from "@/components/ui/button";
import { Collapsible, CollapsibleContent, CollapsibleTrigger } from "@/components/ui/collapsible";
import {
	Dialog,
	DialogContent,
	DialogDescription,
	DialogFooter,
	DialogHeader,
	DialogTitle,
} from "@/components/ui/dialog";
import { ScrollArea } from "@/components/ui/scroll-area";
import { Separator } from "@/components/ui/separator";
import { ticketLibrary } from "@/feature/project/library/ticket-library";
import type { TicketWorkflow } from "@/feature/ticket";

const props = defineProps<{
	tickets: TicketWorkflow[];
	open: boolean;
}>();
const emit = defineEmits<{
	"update:open": [open: boolean];
	importSelected: [items: TicketWorkflow[]];
}>();

const selectedNames = ref(new Set<string>());

const existingNames = computed(() => new Set(props.tickets.map((t) => t.name)));

const availableItems = computed(() =>
	ticketLibrary.filter((item) => !existingNames.value.has(item.name)),
);

const selectionCount = computed(() => selectedNames.value.size);

function toggleSelection(name: string) {
	const next = new Set(selectedNames.value);
	if (next.has(name)) {
		next.delete(name);
	} else {
		next.add(name);
	}
	selectedNames.value = next;
}

function importSelected() {
	const items = ticketLibrary.filter((item) => selectedNames.value.has(item.name));
	emit("importSelected", items);
	selectedNames.value = new Set();
	emit("update:open", false);
}

function onOpenChange(open: boolean) {
	if (!open) {
		selectedNames.value = new Set();
	}
	emit("update:open", open);
}
</script>

<template>
	<Dialog :open="open" @update:open="onOpenChange">
		<DialogContent
			class="max-h-[calc(100vh-2rem)] w-[calc(100vw-2rem)] min-w-0 max-w-4xl overflow-hidden sm:max-w-4xl"
		>
			<DialogHeader>
				<DialogTitle>Ticket-Bibliothek</DialogTitle>
				<DialogDescription>
					Wählen Sie vordefinierte Ticket-Typen und Workflows zum Importieren aus.
				</DialogDescription>
			</DialogHeader>

			<ScrollArea v-if="availableItems.length > 0" class="h-122 min-w-0 max-w-full">
				<div class="flex min-w-0 max-w-full flex-col gap-3 pr-4">
					<template v-for="(item, index) in availableItems" :key="item.name">
						<Separator v-if="index > 0" />
						<div
							class="min-w-0 max-w-full overflow-hidden rounded-lg border transition-colors"
							:class="{
								'border-primary bg-accent/30': selectedNames.has(item.name),
							}"
						>
							<button
								type="button"
								class="flex w-full max-w-full items-center justify-between gap-3 px-3 py-2.5 text-left outline-none transition-colors hover:bg-muted/60 focus-visible:ring-3 focus-visible:ring-ring/50"
								:class="{
									'font-semibold': selectedNames.has(item.name),
								}"
								@click="toggleSelection(item.name)"
							>
								<span class="min-w-0 truncate">{{ item.name }}</span>
								<small class="text-muted-foreground shrink-0">
									{{ item.states.length }} Zustände ·
									{{ item.transitions.length }} Übergänge
								</small>
							</button>
							<div v-if="item.description" class="px-3 pb-3">
								<p class="text-muted-foreground text-sm">{{ item.description }}</p>
							</div>
							<Collapsible class="min-w-0 max-w-full">
								<CollapsibleTrigger
									class="border-t flex w-full px-3 py-2 text-left text-sm font-medium outline-none hover:bg-muted/60 focus-visible:ring-3 focus-visible:ring-ring/50"
								>
									Workflow-JSON anzeigen
								</CollapsibleTrigger>
								<CollapsibleContent
									class="min-w-0 max-w-full overflow-hidden border-t bg-muted/30 px-3 py-2"
								>
									<pre
										class="text-muted-foreground max-w-full overflow-hidden text-xs whitespace-pre-wrap break-all"
										>{{ JSON.stringify(item, null, 2) }}</pre
									>
								</CollapsibleContent>
							</Collapsible>
						</div>
					</template>
				</div>
			</ScrollArea>

			<p v-else class="text-muted-foreground text-sm">
				Alle Bibliotheks-Typen sind bereits im Projekt vorhanden.
			</p>

			<DialogFooter>
				<Button type="button" variant="outline" @click="onOpenChange(false)">
					Abbrechen
				</Button>
				<Button type="button" :disabled="selectionCount === 0" @click="importSelected">
					Importieren ({{ selectionCount }})
				</Button>
			</DialogFooter>
		</DialogContent>
	</Dialog>
</template>
