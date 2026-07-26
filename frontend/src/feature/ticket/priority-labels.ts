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

export function formatTicketPriority(priority: string | null): string {
	return priority === null
		? "?"
		: (TICKET_PRIORITY_LABELS[priority as TicketPriorityName] ?? priority);
}
