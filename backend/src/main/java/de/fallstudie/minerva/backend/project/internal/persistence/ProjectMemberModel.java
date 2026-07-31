package de.fallstudie.minerva.backend.project.internal.persistence;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "project_members")
@Getter
public class ProjectMemberModel {
	@Id
	@SequenceGenerator(name = "project_members_id_seq", sequenceName = "project_members_id_seq", allocationSize = 1)
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "project_members_id_seq")
	private long id;

	@Setter
	private long projectId;

	@Setter
	private long userId;

	@Setter
	private long roleId;
}
