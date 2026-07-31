package de.fallstudie.minerva.backend.ticket.internal.service;

import java.util.Collection;
import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import de.fallstudie.minerva.backend.ticket.ITicketStatisticsService;
import de.fallstudie.minerva.backend.ticket.TicketPriorityName;
import de.fallstudie.minerva.backend.ticket.TicketStatisticsCount;
import de.fallstudie.minerva.backend.ticket.TicketStatisticsRecentTicket;
import de.fallstudie.minerva.backend.ticket.TicketStatusCategory;
import de.fallstudie.minerva.backend.ticket.internal.persistence.TicketRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TicketStatisticsService implements ITicketStatisticsService {
	private final TicketRepository ticketRepository;

	@Override
	public TicketStatisticsCount getTicketCount(Collection<Long> projectIds,
			TicketPriorityName priority, TicketStatusCategory category) {
		return ticketRepository.countForStatistics(projectIds, priority, category);
	}

	@Override
	public List<TicketStatisticsRecentTicket> getRecentTickets(Collection<Long> projectIds,
			int limit) {
		return ticketRepository.findRecentForStatistics(projectIds, PageRequest.of(0, limit));
	}
}
