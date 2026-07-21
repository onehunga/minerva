package de.fallstudie.minerva.backend.user.internal.persistence;

import de.fallstudie.minerva.backend.user.WorkspaceRoleName;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<UserModel, Long> {
	Optional<UserModel> findByUsernameAndDeletedAtIsNull(String username);

	boolean existsByUsernameAndDeletedAtIsNull(String username);

	boolean existsByIdAndDeletedAtIsNull(long id);

	List<UserModel> findAllByDeletedAtIsNull();

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	List<UserModel> findAllByWorkspaceRole_NameAndDeletedAtIsNull(WorkspaceRoleName roleName);
}
