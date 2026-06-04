package de.fallstudie.minerva.backend.ticket.internal.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TicketTypeRepository extends JpaRepository<TicketTypeModel, Long> {
}
