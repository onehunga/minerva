package de.fallstudie.minerva.backend.ticket.internal.persistence;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface WorkflowTransitionRepository extends JpaRepository<WorkflowTransitionModel, Long> {
	@Query("""
			SELECT t FROM WorkflowTransitionModel t
			WHERE t.fromState IN :stateIds
			   OR (t.fromState IS NULL AND t.toState IN :stateIds)
			ORDER BY t.id ASC
			""")
	List<WorkflowTransitionModel> findAllForWorkflowStates(
			@Param("stateIds") Collection<Long> stateIds);
}
