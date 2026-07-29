package de.fallstudie.minerva.backend.notification.internal.persistence;

import de.fallstudie.minerva.backend.notification.internal.NotificationType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "notifications")
@Getter
@Setter
@NoArgsConstructor
public class NotificationModel {
	@Id
	@SequenceGenerator(name = "notifications_id_seq", sequenceName = "notifications_id_seq", allocationSize = 1)
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "notifications_id_seq")
	private long id;

	@Column(nullable = false)
	private long recipientUserId;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private NotificationType type;

	@Column(nullable = false, columnDefinition = "TEXT")
	private String payloadJson;

	@Column(nullable = false, updatable = false)
	private Instant createdAt;

	private Instant readAt;
}
