package de.fallstudie.minerva.backend.ticket.internal.persistence;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "workflow_transitions")
@Getter
@NoArgsConstructor
public class WorkflowTransitionModel {
	@Id
	@SequenceGenerator(name = "workflow_transitions_id_seq", sequenceName = "workflow_transitions_id_seq", allocationSize = 1)
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "workflow_transitions_id_seq")
	private long id;

	@Setter
	private String name;

	@Setter
	private long fromState;

	@Setter
	@Column(nullable = false)
	private long toState;
}
