package de.fallstudie.minerva.backend.ticket.internal.persistence;

import jakarta.persistence.*;

@Entity
@Table(name = "workflow_transitions")
public class WorkflowTransitionModel {
	@Id
	@SequenceGenerator(name = "workflow_transitions_id_seq", sequenceName = "workflow_transitions_id_seq", allocationSize = 1)
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "workflow_transitions_id_seq")
	private long id;

	private String name;

	private long fromState;

	@Column(nullable = false)
	private long toState;
}
