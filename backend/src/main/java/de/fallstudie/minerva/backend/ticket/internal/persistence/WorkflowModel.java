package de.fallstudie.minerva.backend.ticket.internal.persistence;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "workflows")
@Getter
@NoArgsConstructor
public class WorkflowModel {
	@Id
	@SequenceGenerator(name = "workflows_id_seq", sequenceName = "workflows_id_seq", allocationSize = 1)
	@GeneratedValue(strategy = GenerationType.IDENTITY, generator = "workflows_id_seq")
	private long id;

	@Setter
	@Column(nullable = false)
	private long projectId;

	@Setter
	@Column(nullable = false)
	private long ticketTypeId;
}
