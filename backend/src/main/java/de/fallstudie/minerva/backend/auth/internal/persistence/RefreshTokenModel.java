package de.fallstudie.minerva.backend.auth.internal.persistence;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "refresh_tokens")
@Getter
@NoArgsConstructor
public class RefreshTokenModel {
	@Id
	@SequenceGenerator(name = "refresh_tokens_id_seq", sequenceName = "refresh_tokens_id_seq", initialValue = 1, allocationSize = 1)
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "refresh_tokens_id_seq")
	private Long id;

	@Setter
	@Column(name = "user_id", nullable = false)
	private long userId;

	@Setter
	@Column(name = "token_hash", nullable = false, unique = true, length = 64)
	private String tokenHash;

	@Setter
	@Column(name = "expires_at", nullable = false)
	private Instant expiresAt;

	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt = Instant.now();
}
