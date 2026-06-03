package de.fallstudie.minerva.backend.ticket.internal.persistence;

import jakarta.persistence.*;

@Entity
@Table(name = "ticket_types")
public class TicketTypeModel {
	@Id
	@SequenceGenerator(name = "ticket_types_id_seq", sequenceName = "ticket_types_id_seq", allocationSize = 1)
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "ticket_types_id_seq")
	private long id;

	@Column(nullable = false, updatable = false)
	private long projectId;

	@Column(nullable = false)
	private String name;

	@Column(nullable = false)
	private String description;
}
