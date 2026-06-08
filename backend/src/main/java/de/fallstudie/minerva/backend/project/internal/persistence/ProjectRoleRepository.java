package de.fallstudie.minerva.backend.project.internal.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectRoleRepository extends JpaRepository<ProjectRoleModel, Long> {
	List<ProjectRoleModel> findAllByProjectId(long projectId);

	Optional<ProjectRoleModel> findByProjectIdAndName(long projectId, ProjectRoleName name);
}
