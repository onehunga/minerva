import { inject } from "vue";
import { TicketRepositoryKey, type ITicketRepository } from "../ticket.repository";

export function useTicketRepository(): ITicketRepository {
	const repo = inject<ITicketRepository>(TicketRepositoryKey);
	if (repo === undefined) {
		throw new Error("TicketRepository not provided");
	}

	return repo;
}
