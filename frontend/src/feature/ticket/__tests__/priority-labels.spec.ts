import { describe, expect, it } from "vitest";
import { formatTicketPriority, ticketPriorityIndicatorClass } from "../priority-labels";

describe("formatTicketPriority", () => {
	it("uses the canonical labels and keeps unknown values readable", () => {
		expect(formatTicketPriority("LOWEST")).toBe("Niedrigste");
		expect(formatTicketPriority("HIGHEST")).toBe("Höchste");
		expect(formatTicketPriority("UNKNOWN")).toBe("UNKNOWN");
		expect(ticketPriorityIndicatorClass("LOWEST")).not.toBe(
			ticketPriorityIndicatorClass("HIGHEST"),
		);
		expect(formatTicketPriority(null)).toBe("?");
	});
});
