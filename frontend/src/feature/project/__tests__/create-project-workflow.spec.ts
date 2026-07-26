import { describe, expect, it } from "vitest";
import type { TicketWorkflow } from "@/feature/ticket";
import {
	getWorkflowTransitionTargets,
	groupWorkflowTransitions,
	isWorkflowConnectionAvailable,
	removeTicketWorkflow,
	replaceTicketWorkflow,
} from "../create-project-workflow";

function workflow(
	name: string,
	children: string[] = [],
	transitions: TicketWorkflow["transitions"] = [],
): TicketWorkflow {
	return {
		name,
		description: "",
		states: [
			{ name: "Offen", category: "OPEN" },
			{ name: "Erledigt", category: "COMPLETED" },
		],
		transitions,
		children,
	};
}

describe("create project workflow", () => {
	it("updates references when a ticket type is renamed or removed", () => {
		const tickets = new Map([
			["Aufgabe", workflow("Aufgabe", ["Fehler"])],
			["Fehler", workflow("Fehler")],
		]);

		const renamed = replaceTicketWorkflow(tickets, "Fehler", workflow("Bug"));
		expect([...renamed.keys()]).toEqual(["Aufgabe", "Bug"]);
		expect(renamed.get("Aufgabe")?.children).toEqual(["Bug"]);

		const removed = removeTicketWorkflow(renamed, "Bug");
		expect([...removed.keys()]).toEqual(["Aufgabe"]);
		expect(removed.get("Aufgabe")?.children).toEqual([]);
	});

	it("groups transitions and rejects duplicate or self-referencing connections", () => {
		const transition = { name: "Abschließen", from: "Offen", to: "Erledigt" };
		const ticketWorkflow = workflow("Aufgabe", [], [transition]);

		expect(groupWorkflowTransitions(ticketWorkflow)).toEqual([
			{ name: "Abschließen", connections: [transition] },
		]);
		expect(isWorkflowConnectionAvailable(ticketWorkflow, "Offen", "Erledigt")).toBe(false);
		expect(isWorkflowConnectionAvailable(ticketWorkflow, "Offen", "Offen")).toBe(false);
		expect(getWorkflowTransitionTargets(ticketWorkflow, null)).toEqual(["Offen", "Erledigt"]);
	});
});
