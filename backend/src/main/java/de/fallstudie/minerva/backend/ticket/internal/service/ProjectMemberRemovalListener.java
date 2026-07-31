package de.fallstudie.minerva.backend.ticket.internal.service;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import de.fallstudie.minerva.backend.project.ProjectEvent;
import de.fallstudie.minerva.backend.ticket.TicketEvent;
import de.fallstudie.minerva.backend.ticket.internal.persistence.TicketRepository;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ProjectMemberRemovalListener {
	private final TicketRepository ticketRepository;
	private final ApplicationEventPublisher eventPublisher;

	@EventListener
	public void on(ProjectEvent.UserRemoved event) {
		for (var ticket : ticketRepository.findAllByProjectIdAndAssignedTo(event.projectId(),
				event.userId())) {
			ticket.setAssignedTo(null);
			ticketRepository.save(ticket);
			eventPublisher.publishEvent(new TicketEvent.AssigneeChanged(event.actorUserId(),
					event.projectId(), ticket.getId(), event.userId(), null));
		}
	}
}
