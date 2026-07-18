import type { TicketPriorityName } from "./ticket.model";

export const TICKET_PRIORITY_ORDER: TicketPriorityName[] = [
	"LOWEST",
	"LOW",
	"NORMAL",
	"HIGH",
	"HIGHEST",
];

export const TICKET_PRIORITY_LABELS: Record<TicketPriorityName, string> = {
	LOWEST: "Niedrigste",
	LOW: "Niedrig",
	NORMAL: "Normal",
	HIGH: "Hoch",
	HIGHEST: "Höchste",
};

export function formatTicketPriority(priority: TicketPriorityName): string {
	return TICKET_PRIORITY_LABELS[priority];
}
