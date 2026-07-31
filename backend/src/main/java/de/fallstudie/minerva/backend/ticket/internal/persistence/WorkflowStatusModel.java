package de.fallstudie.minerva.backend.ticket.internal.persistence;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import de.fallstudie.minerva.backend.ticket.TicketStatusCategory;

@Entity
@Table(name = "workflow_states")
@Getter
@NoArgsConstructor
public class WorkflowStatusModel {
	@Id
	@SequenceGenerator(name = "workflow_states_id_seq", sequenceName = "workflow_states_id_seq", allocationSize = 1)
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "workflow_states_id_seq")
	private long id;

	@Setter
	@Column(nullable = false)
	private long workflowId;

	@Setter
	@Column(nullable = false)
	private String name;

	@Setter
	@Column(nullable = false)
	@Enumerated(EnumType.STRING)
	private TicketStatusCategory workflowStatusCategory;
}
