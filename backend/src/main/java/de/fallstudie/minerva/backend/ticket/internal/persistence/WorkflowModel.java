package de.fallstudie.minerva.backend.ticket.internal.persistence;

import jakarta.persistence.*;

@Entity
@Table(name = "workflows")
public class WorkflowModel {
	@Id
	@SequenceGenerator(name = "workflows_id_seq", sequenceName = "workflows_id_seq", allocationSize = 1)
	@GeneratedValue(strategy = GenerationType.IDENTITY, generator = "workflows_id_seq")
	private long id;

	@Column(nullable = false)
	private long projectId;

	@Column(nullable = false)
	private long ticketTypeId;
}
