package de.fallstudie.minerva.backend.activity.internal.persistence;

import java.time.Instant;

import de.fallstudie.minerva.backend.activity.ActivityScopeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "activity_scopes")
@Getter
@Setter
@NoArgsConstructor
public class ActivityScopeModel {
	@Id
	@SequenceGenerator(name = "activity_scopes_id_seq", sequenceName = "activity_scopes_id_seq", allocationSize = 1)
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "activity_scopes_id_seq")
	private long id;

	@Column(nullable = false)
	private long eventId;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private ActivityScopeType scopeType;

	@Column(nullable = false)
	private long scopeId;

	@Column(nullable = false, updatable = false)
	private Instant occurredAt;
}
