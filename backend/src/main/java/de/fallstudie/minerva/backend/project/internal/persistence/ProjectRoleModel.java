package de.fallstudie.minerva.backend.project.internal.persistence;

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
@Table(name = "project_roles")
@Getter
@NoArgsConstructor
public class ProjectRoleModel {
	@Id
	@SequenceGenerator(name = "project_roles_id_seq", sequenceName = "project_roles_id_seq", allocationSize = 1)
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "project_roles_id_seq")
	private long id;

	@Setter
	private long projectId;

	@Setter
	@Enumerated(EnumType.STRING)
	private ProjectRoleName name;
}
