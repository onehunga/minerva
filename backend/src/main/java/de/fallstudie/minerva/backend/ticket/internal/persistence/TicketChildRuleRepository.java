package de.fallstudie.minerva.backend.ticket.internal.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TicketChildRuleRepository extends JpaRepository<TicketChildRuleModel, Long> {
	List<TicketChildRuleModel> findAllByParentTicketIdOrderByIdAsc(long parentTicketId);

	Optional<TicketChildRuleModel> findByParentTicketIdAndChildTicketId(long parentTicketId,
			long childTicketId);
}
