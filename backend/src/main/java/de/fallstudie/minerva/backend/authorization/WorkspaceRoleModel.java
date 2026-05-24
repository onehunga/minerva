package de.fallstudie.minerva.backend.authorization;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "workspace_roles")
@Getter
@NoArgsConstructor
public class WorkspaceRoleModel {
	@Id
	@SequenceGenerator(name = "workspace_role_seq", sequenceName = "workspace_roles_seq")
	@GeneratedValue(generator = "workspace_role_seq")
	private Long id;

	@Column(nullable = false, unique = true)
	@Enumerated(EnumType.STRING)
	private WorkspaceRoleName name;
}
