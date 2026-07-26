import type { TicketWorkflow } from "@/feature/ticket";

export type WorkflowTransition = TicketWorkflow["transitions"][number];

export function groupWorkflowTransitions(
	workflow: TicketWorkflow,
): Array<{ name: string; connections: WorkflowTransition[] }> {
	const groups = new Map<string, WorkflowTransition[]>();

	for (const transition of workflow.transitions) {
		const connections = groups.get(transition.name) ?? [];
		connections.push(transition);
		groups.set(transition.name, connections);
	}

	return Array.from(groups, ([name, connections]) => ({ name, connections }));
}

export function isWorkflowStateReferenced(workflow: TicketWorkflow, stateName: string): boolean {
	return workflow.transitions.some(
		(transition) => transition.from === stateName || transition.to === stateName,
	);
}

export function isWorkflowConnectionAvailable(
	workflow: TicketWorkflow,
	from: string | null,
	to: string,
	current?: WorkflowTransition,
): boolean {
	return (
		from !== to &&
		!workflow.transitions.some(
			(transition) =>
				transition !== current && transition.from === from && transition.to === to,
		)
	);
}

export function getWorkflowTransitionTargets(
	workflow: TicketWorkflow,
	from: string | null,
	current?: WorkflowTransition,
): string[] {
	return workflow.states
		.map((state) => state.name)
		.filter((to) => isWorkflowConnectionAvailable(workflow, from, to, current));
}

export function removeTicketWorkflow(
	tickets: ReadonlyMap<string, TicketWorkflow>,
	ticketName: string,
): Map<string, TicketWorkflow> {
	const updatedTickets = new Map(tickets);
	updatedTickets.delete(ticketName);

	for (const [name, workflow] of updatedTickets) {
		if (workflow.children.includes(ticketName)) {
			updatedTickets.set(name, {
				...workflow,
				children: workflow.children.filter((child) => child !== ticketName),
			});
		}
	}

	return updatedTickets;
}

export function replaceTicketWorkflow(
	tickets: ReadonlyMap<string, TicketWorkflow>,
	oldName: string,
	updatedWorkflow: TicketWorkflow,
): Map<string, TicketWorkflow> {
	return new Map(
		Array.from(tickets, ([name, workflow]) => {
			const nextWorkflow = name === oldName ? updatedWorkflow : workflow;
			return [
				name === oldName ? updatedWorkflow.name : name,
				{
					...nextWorkflow,
					children: nextWorkflow.children.map((child) =>
						child === oldName ? updatedWorkflow.name : child,
					),
				},
			] as const;
		}),
	);
}
