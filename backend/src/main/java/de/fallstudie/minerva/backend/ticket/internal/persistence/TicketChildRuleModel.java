package de.fallstudie.minerva.backend.ticket.internal.persistence;

import jakarta.persistence.*;

@Entity
@Table(name = "ticket_child_rules")
public class TicketChildRuleModel {
	@Id
	@SequenceGenerator(name = "ticket_child_rule_id_seq", sequenceName = "ticket_child_rule_id_seq", allocationSize = 1)
	@GeneratedValue(strategy = GenerationType.IDENTITY, generator = "ticket_child_rule_id_seq")
	private long id;

	@Column(nullable = false)
	private long parentTicketId;

	@Column(nullable = false)
	private long childTicketId;
}
