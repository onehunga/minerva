import { ref } from "vue";
import type { CreateTicketType, TicketStatusCategory } from "..";

// eslint-disable-next-line @typescript-eslint/explicit-module-boundary-types
export function useConfigureTickets(initialTickets: CreateTicketType[] = []) {
	const ticketTypes = ref<Map<string, CreateTicketType>>(new Map());

	initialTickets.forEach((ticket) => {
		ticketTypes.value.set(ticket.name, ticket);
	});

	function createTicketType(name: string, description: string): boolean {
		if (ticketTypes.value.has(name)) {
			return false; // Ticket type already exists
		}

		ticketTypes.value.set(name, {
			name,
			description,
			states: new Map(),
			transitions: [],
			children: new Set(),
		});
		return true;
	}

	function addNewTicketState(
		ticketTypeName: string,
		stateName: string,
		statusCategory: TicketStatusCategory,
	): boolean {
		const ticketType = ticketTypes.value.get(ticketTypeName);
		if (!ticketType) {
			return false; // Ticket type does not exist
		}

		if (ticketType.states.has(stateName)) {
			return false; // State already exists
		}

		ticketType.states.set(stateName, { name: stateName, statusCategory });
		return true;
	}

	function addNewTicketTransition(
		ticketTypeName: string,
		transitionName: string,
		fromState: string,
		toState: string,
	): boolean {
		const ticketType = ticketTypes.value.get(ticketTypeName);
		if (!ticketType) {
			return false; // Ticket type does not exist
		}

		if (!ticketType.states.has(fromState) || !ticketType.states.has(toState)) {
			return false; // One of the states does not exist
		}

		if (
			ticketType.transitions.some(
				(t) =>
					t.name === transitionName ||
					(t.fromState === fromState && t.toState === toState),
			)
		) {
			return false; // Transition already exists
		}

		ticketType.transitions.push({ name: transitionName, fromState, toState });
		return true;
	}

	function addNewTicketChild(ticketTypeName: string, childTypeName: string) {
		const ticketType = ticketTypes.value.get(ticketTypeName);
		const childType = ticketTypes.value.get(childTypeName);

		if (!ticketType || !childType) {
			return false; // One of the ticket types does not exist
		}

		if (ticketType.children.has(childTypeName)) {
			return false; // Child type already exists
		}

		ticketType.children.add(childTypeName);
		return true;
	}

	return {
		ticketTypes,
		createTicketType,
		addNewTicketState,
		addNewTicketTransition,
		addNewTicketChild,
	};
}
