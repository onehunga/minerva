package de.fallstudie.minerva.backend.user.internal.persistence;

import java.time.Instant;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "users")
@NoArgsConstructor
@Getter
public class UserModel {
	@Id
	@SequenceGenerator(name = "users_id_seq", sequenceName = "users_id_seq", allocationSize = 1, initialValue = 1)
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "users_id_seq")
	private Long id;

	@Setter
	@Column(unique = true)
	private String username;

	@Setter
	@Column(nullable = false)
	private String password;

	@Setter
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "workspace_role_id")
	private WorkspaceRoleModel workspaceRole;

	@Setter
	@Column(name = "deleted_at")
	private Instant deletedAt;

	@Setter
	@Column(name = "deactivated_at")
	private Instant deactivatedAt;
}
