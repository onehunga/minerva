import type { TicketPriorityName } from "./ticket.model";

export const TICKET_PRIORITY_ORDER: TicketPriorityName[] = [
	"LOWEST",
	"LOW",
	"NORMAL",
	"HIGH",
	"HIGHEST",
];

const TICKET_PRIORITY_LABELS: Record<TicketPriorityName, string> = {
	LOWEST: "Niedrigste",
	LOW: "Niedrig",
	NORMAL: "Normal",
	HIGH: "Hoch",
	HIGHEST: "Höchste",
};

const TICKET_PRIORITY_INDICATOR_CLASSES: Record<TicketPriorityName, string> = {
	LOWEST: "bg-slate-400",
	LOW: "bg-sky-500",
	NORMAL: "bg-emerald-500",
	HIGH: "bg-amber-500",
	HIGHEST: "bg-red-500",
};

export function formatTicketPriority(priority: string | null): string {
	return priority === null
		? "?"
		: (TICKET_PRIORITY_LABELS[priority as TicketPriorityName] ?? priority);
}

export function ticketPriorityIndicatorClass(priority: TicketPriorityName): string {
	return TICKET_PRIORITY_INDICATOR_CLASSES[priority];
}
