package de.fallstudie.minerva.backend.ticket.internal.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import de.fallstudie.minerva.backend.ticket.TicketPriorityName;
import de.fallstudie.minerva.backend.ticket.TicketStatusCategory;

@DataJpaTest(properties = {"spring.jpa.hibernate.ddl-auto=create-drop",
		"spring.liquibase.enabled=false"})
@ActiveProfiles("test")
class TicketRepositoryStatisticsTests {
	private static final long PROJECT_ID = 7L;
	private static final long TICKET_TYPE_ID = 3L;

	@Autowired
	private TicketRepository ticketRepository;

	@Autowired
	private WorkflowRepository workflowRepository;

	@Autowired
	private WorkflowStatusRepository workflowStatusRepository;

	@Test
	void returnsTypedCountAndRecentTicketProjections() {
		final var workflow = new WorkflowModel();
		workflow.setProjectId(PROJECT_ID);
		workflow.setTicketTypeId(TICKET_TYPE_ID);
		workflowRepository.saveAndFlush(workflow);

		final var status = new WorkflowStatusModel();
		status.setWorkflowId(workflow.getId());
		status.setName("Offen");
		status.setWorkflowStatusCategory(TicketStatusCategory.OPEN);
		workflowStatusRepository.saveAndFlush(status);

		final var ticket = new TicketModel();
		ticket.setProjectId(PROJECT_ID);
		ticket.setTicketTypeId(TICKET_TYPE_ID);
		ticket.setStatusId(status.getId());
		ticket.setName("Ticket");
		ticket.setDescription("Beschreibung");
		ticket.setCreatedBy(1L);
		ticket.setPriority(TicketPriorityName.HIGH);
		ticketRepository.saveAndFlush(ticket);

		final var count = ticketRepository.countForStatistics(List.of(PROJECT_ID),
				TicketPriorityName.HIGH, TicketStatusCategory.OPEN);
		final var recent = ticketRepository.findRecentForStatistics(List.of(PROJECT_ID),
				org.springframework.data.domain.PageRequest.of(0, 5));

		assertEquals(TicketPriorityName.HIGH, count.priority());
		assertEquals(TicketStatusCategory.OPEN, count.category());
		assertEquals(1L, count.count());
		assertEquals(1, recent.size());
		assertEquals(ticket.getId(), recent.getFirst().id());
		assertEquals("Offen", recent.getFirst().statusName());
	}
}
