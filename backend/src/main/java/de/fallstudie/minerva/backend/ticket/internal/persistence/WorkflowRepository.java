package de.fallstudie.minerva.backend.ticket.internal.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkflowRepository extends JpaRepository<WorkflowModel, Long> {
}
