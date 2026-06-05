package de.fallstudie.minerva.backend.ticket.internal.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TicketTypeRepository extends JpaRepository<TicketTypeModel, Long> {
	List<TicketTypeModel> findAllByProjectIdOrderByNameAsc(long projectId);

	Optional<TicketTypeModel> findByIdAndProjectId(long id, long projectId);
}
