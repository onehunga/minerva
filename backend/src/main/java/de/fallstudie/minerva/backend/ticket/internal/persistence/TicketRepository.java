package de.fallstudie.minerva.backend.ticket.internal.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TicketRepository extends JpaRepository<TicketModel, Long> {
	List<TicketModel> findAllByProjectIdOrderByNameAsc(long projectId);

	Optional<TicketModel> findByIdAndProjectId(long id, long projectId);
}
