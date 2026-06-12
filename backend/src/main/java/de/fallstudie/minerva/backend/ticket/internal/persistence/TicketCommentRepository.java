package de.fallstudie.minerva.backend.ticket.internal.persistence;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TicketCommentRepository extends JpaRepository<TicketCommentModel, Long> {
	List<TicketCommentModel> findAllByTicketIdOrderByCreatedAtAscIdAsc(long ticketId);

	void deleteAllByTicketId(long ticketId);
}
