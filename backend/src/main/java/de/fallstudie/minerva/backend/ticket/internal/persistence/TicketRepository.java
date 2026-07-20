package de.fallstudie.minerva.backend.ticket.internal.persistence;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import de.fallstudie.minerva.backend.ticket.TicketPriorityName;
import de.fallstudie.minerva.backend.ticket.TicketStatisticsCount;
import de.fallstudie.minerva.backend.ticket.TicketStatisticsRecentTicket;
import de.fallstudie.minerva.backend.ticket.TicketStatusCategory;

public interface TicketRepository extends JpaRepository<TicketModel, Long> {
	List<TicketModel> findAllByProjectIdOrderByNameAsc(long projectId);

	Optional<TicketModel> findByIdAndProjectId(long id, long projectId);

	boolean existsByParentTicketId(long parentTicketId);

	@Query("""
			select new de.fallstudie.minerva.backend.ticket.TicketStatisticsCount(
				:priority, :category, count(t.id))
			from TicketModel t
			join WorkflowModel wf on wf.projectId = t.projectId
				and wf.ticketTypeId = t.ticketTypeId
			join WorkflowStatusModel ws on ws.id = t.statusId and ws.workflowId = wf.id
			where t.projectId in :projectIds
				and t.priority = :priority
				and ws.workflowStatusCategory = :category
			""")
	TicketStatisticsCount countForStatistics(@Param("projectIds") Collection<Long> projectIds,
			@Param("priority") TicketPriorityName priority,
			@Param("category") TicketStatusCategory category);

	@Query("""
			select new de.fallstudie.minerva.backend.ticket.TicketStatisticsRecentTicket(
				t.id, t.projectId, t.name, t.priority, ws.name, ws.workflowStatusCategory,
				t.createdAt)
			from TicketModel t
			join WorkflowModel wf on wf.projectId = t.projectId
				and wf.ticketTypeId = t.ticketTypeId
			join WorkflowStatusModel ws on ws.id = t.statusId and ws.workflowId = wf.id
			where t.projectId in :projectIds
			order by t.createdAt desc, t.id desc
			""")
	List<TicketStatisticsRecentTicket> findRecentForStatistics(
			@Param("projectIds") Collection<Long> projectIds, Pageable pageable);
}
