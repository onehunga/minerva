package de.fallstudie.minerva.backend.ticket.internal.persistence;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

@Entity
@Table(name = "tickets")
@Getter
public class TicketModel {
	@Id
	@SequenceGenerator(name = "tickets_id_seq", sequenceName = "tickets_id_seq", allocationSize = 1)
	@GeneratedValue(strategy = GenerationType.IDENTITY, generator = "tickets_id_seq")
	private long id;

	@Setter
	@Column(nullable = false)
	private long projectId;

	@Setter
	@Column(nullable = false)
	private long ticketTypeId;

	@Setter
	@Column(nullable = false)
	private long statusId;

	@Setter
	@Column(nullable = false)
	private String name;

	@Setter
	@Column(nullable = false)
	private String description;

	@Setter
	@Column(nullable = false, updatable = false)
	private long createdBy;

	@Setter
	private Long assignedTo = null;

	@CreationTimestamp
	@Column(nullable = false, updatable = false)
	private Instant createdAt;

	@UpdateTimestamp
	@Column(nullable = false)
	private Instant updatedAt;
}
