package de.fallstudie.minerva.backend.ticket;

import java.util.Collection;
import java.util.List;

public interface ITicketStatisticsService {
	TicketStatisticsCount getTicketCount(Collection<Long> projectIds, TicketPriorityName priority,
			TicketStatusCategory category);

	List<TicketStatisticsRecentTicket> getRecentTickets(Collection<Long> projectIds, int limit);
}
