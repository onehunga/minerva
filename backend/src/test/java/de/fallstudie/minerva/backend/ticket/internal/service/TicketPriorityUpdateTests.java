package de.fallstudie.minerva.backend.ticket.internal.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import de.fallstudie.minerva.backend.common.ResourceNotFoundException;
import de.fallstudie.minerva.backend.common.ValidationException;
import de.fallstudie.minerva.backend.testsupport.TestProjects;
import de.fallstudie.minerva.backend.ticket.TicketPriorityName;
import de.fallstudie.minerva.backend.ticket.internal.persistence.TicketChildRuleRepository;
import de.fallstudie.minerva.backend.ticket.internal.persistence.TicketModel;
import de.fallstudie.minerva.backend.ticket.internal.persistence.TicketRepository;
import de.fallstudie.minerva.backend.ticket.internal.persistence.TicketTypeRepository;
import de.fallstudie.minerva.backend.ticket.internal.persistence.WorkflowRepository;
import de.fallstudie.minerva.backend.ticket.internal.persistence.WorkflowStatusRepository;
import de.fallstudie.minerva.backend.ticket.internal.persistence.WorkflowTransitionRepository;
import de.fallstudie.minerva.backend.ticket.internal.web.UpdateTicketPriorityRequest;

class TicketPriorityUpdateTests {
	private TicketTypeRepository ticketTypeRepository;
	private TicketChildRuleRepository ticketChildRuleRepository;
	private WorkflowRepository workflowRepository;
	private WorkflowStatusRepository workflowStatusRepository;
	private WorkflowTransitionRepository workflowTransitionRepository;
	private TicketRepository ticketRepository;
	private TicketService ticketService;

	@BeforeEach
	void setUp() {
		ticketTypeRepository = Mockito.mock(TicketTypeRepository.class);
		ticketChildRuleRepository = Mockito.mock(TicketChildRuleRepository.class);
		workflowRepository = Mockito.mock(WorkflowRepository.class);
		workflowStatusRepository = Mockito.mock(WorkflowStatusRepository.class);
		workflowTransitionRepository = Mockito.mock(WorkflowTransitionRepository.class);
		ticketRepository = Mockito.mock(TicketRepository.class);
		ticketService = new TicketService(ticketTypeRepository, ticketChildRuleRepository,
				workflowRepository, workflowStatusRepository, workflowTransitionRepository,
				ticketRepository);
	}

	@Test
	void updateTicketPrioritySavesPriority() {
		final var ticket = TestProjects.ticket(TestProjects.CHILD_TICKET_ID,
				TestProjects.PROJECT_ID, TestProjects.CHILD_TICKET_TYPE_ID,
				TestProjects.OPEN_STATUS_ID);
		when(ticketRepository.findByIdAndProjectId(ticket.getId(), TestProjects.PROJECT_ID))
				.thenReturn(Optional.of(ticket));

		ticketService.updateTicketPriority(TestProjects.PROJECT_ID, ticket.getId(),
				new UpdateTicketPriorityRequest(TicketPriorityName.HIGH));

		final var ticketCaptor = ArgumentCaptor.forClass(TicketModel.class);
		verify(ticketRepository).save(ticketCaptor.capture());
		assertEquals(TicketPriorityName.HIGH, ticketCaptor.getValue().getPriority());
	}

	@Test
	void updateTicketPriorityRejectsUnknownTicket() {
		when(ticketRepository.findByIdAndProjectId(TestProjects.CHILD_TICKET_ID,
				TestProjects.PROJECT_ID)).thenReturn(Optional.empty());

		assertThrows(ResourceNotFoundException.class,
				() -> ticketService.updateTicketPriority(TestProjects.PROJECT_ID,
						TestProjects.CHILD_TICKET_ID,
						new UpdateTicketPriorityRequest(TicketPriorityName.HIGH)));

		verify(ticketRepository, never()).save(any());
	}

	@Test
	void updateTicketPriorityRejectsNullPriority() {
		assertThrows(ValidationException.class,
				() -> ticketService.updateTicketPriority(TestProjects.PROJECT_ID,
						TestProjects.CHILD_TICKET_ID, new UpdateTicketPriorityRequest(null)));

		verify(ticketRepository, never()).save(any());
	}

	@Test
	void updateTicketPriorityRejectsNullRequest() {
		assertThrows(IllegalArgumentException.class, () -> ticketService
				.updateTicketPriority(TestProjects.PROJECT_ID, TestProjects.CHILD_TICKET_ID, null));

		verify(ticketRepository, never()).save(any());
	}
}
