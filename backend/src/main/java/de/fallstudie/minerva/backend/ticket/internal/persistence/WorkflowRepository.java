package de.fallstudie.minerva.backend.ticket.internal.persistence;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkflowRepository extends JpaRepository<WorkflowModel, Long> {
	Optional<WorkflowModel> findByProjectIdAndTicketTypeId(long projectId, long ticketTypeId);
}
