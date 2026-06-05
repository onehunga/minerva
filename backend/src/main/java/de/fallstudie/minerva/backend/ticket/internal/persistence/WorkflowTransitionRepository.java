package de.fallstudie.minerva.backend.ticket.internal.persistence;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkflowTransitionRepository extends JpaRepository<WorkflowTransitionModel, Long> {
	List<WorkflowTransitionModel> findAllByFromStateInOrderByIdAsc(Collection<Long> fromStates);
}
