package de.fallstudie.minerva.backend.authorization.internal;

import de.fallstudie.minerva.backend.authorization.WorkspaceRoleModel;
import de.fallstudie.minerva.backend.authorization.WorkspaceRoleName;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WorkspaceRoleRepository extends JpaRepository<WorkspaceRoleModel, Long> {
	Optional<WorkspaceRoleModel> findByName(WorkspaceRoleName name);
}
