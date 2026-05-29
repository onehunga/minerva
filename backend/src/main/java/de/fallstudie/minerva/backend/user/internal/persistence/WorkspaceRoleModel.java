package de.fallstudie.minerva.backend.user.internal.persistence;

import de.fallstudie.minerva.backend.user.WorkspaceRoleName;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "workspace_roles")
@Getter
@NoArgsConstructor
public class WorkspaceRoleModel {
	@Id
	@SequenceGenerator(name = "workspace_roles_id_seq", sequenceName = "workspace_roles_id_seq", initialValue = 1, allocationSize = 1)
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "workspace_roles_id_seq")
	private Long id;

	@Column(nullable = false, unique = true)
	@Enumerated(EnumType.STRING)
	private WorkspaceRoleName name;
}
