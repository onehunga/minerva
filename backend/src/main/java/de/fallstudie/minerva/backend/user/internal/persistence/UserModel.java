package de.fallstudie.minerva.backend.user.internal.persistence;

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
	@Column(nullable = false, unique = true)
	private String username;

	@Setter
	@Column(nullable = false)
	private String password;

	@Setter
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "workspace_role_id")
	private WorkspaceRoleModel workspaceRole;
}
