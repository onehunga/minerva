package de.fallstudie.minerva.backend.user;

import de.fallstudie.minerva.backend.authorization.WorkspaceRoleModel;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor
public class UserModel {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
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
