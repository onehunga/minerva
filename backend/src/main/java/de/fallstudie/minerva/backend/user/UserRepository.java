package de.fallstudie.minerva.backend.user;

import de.fallstudie.minerva.backend.authorization.WorkspaceRoleName;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<UserModel, Long> {
	Optional<UserModel> findByUsername(String username);

	boolean existsByUsername(String username);

	long countByWorkspaceRole_Name(WorkspaceRoleName roleName);
}
