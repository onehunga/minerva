package de.fallstudie.minerva.backend.ticket.internal.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkflowStatusRepository extends JpaRepository<WorkflowStatusModel, Long> {
	List<WorkflowStatusModel> findAllByWorkflowIdOrderByIdAsc(long workflowId);

	Optional<WorkflowStatusModel> findByIdAndWorkflowId(long id, long workflowId);
}
