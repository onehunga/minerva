package de.fallstudie.minerva.backend.ticket.internal.persistence;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "ticket_types")
@Getter
@NoArgsConstructor
public class TicketTypeModel {
	@Id
	@SequenceGenerator(name = "ticket_types_id_seq", sequenceName = "ticket_types_id_seq", allocationSize = 1)
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "ticket_types_id_seq")
	private long id;

	@Setter
	@Column(nullable = false, updatable = false)
	private long projectId;

	@Setter
	@Column(nullable = false)
	private String name;

	@Setter
	@Column(nullable = false)
	private String description;
}
