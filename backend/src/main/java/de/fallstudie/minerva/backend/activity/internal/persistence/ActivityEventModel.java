package de.fallstudie.minerva.backend.activity.internal.persistence;

import de.fallstudie.minerva.backend.activity.ActivityEventType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "activity_events")
@Getter
@Setter
@NoArgsConstructor
public class ActivityEventModel {
	@Id
	@SequenceGenerator(name = "activity_events_id_seq", sequenceName = "activity_events_id_seq", allocationSize = 1)
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "activity_events_id_seq")
	private long id;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private ActivityEventType type;

	@Column(nullable = false)
	private int schemaVersion;

	private Long actorUserId;

	@Column(nullable = false, columnDefinition = "TEXT")
	private String payloadJson;

	@Column(nullable = false, updatable = false)
	private Instant occurredAt;
}
