package de.fallstudie.minerva.backend.ticket.internal.persistence;

import jakarta.persistence.*;

@Entity
@Table(name = "workflow_states")
public class WorkflowStatusModel {
	@Id
	@SequenceGenerator(name = "workflow_states_id_seq", sequenceName = "workflow_states_id_seq", allocationSize = 1)
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "workflow_states_id_seq")
	private long id;

	@Column(nullable = false)
	private long workflowId;

	@Column(nullable = false)
	private String name;

	@Column(nullable = false)
	@Enumerated(EnumType.STRING)
	private WorkflowStatusCategory workflowStatusCategory;
}
