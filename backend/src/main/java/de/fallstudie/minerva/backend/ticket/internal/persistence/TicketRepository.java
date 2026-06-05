package de.fallstudie.minerva.backend.ticket.internal.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TicketRepository extends JpaRepository<TicketModel, Long> {
}
