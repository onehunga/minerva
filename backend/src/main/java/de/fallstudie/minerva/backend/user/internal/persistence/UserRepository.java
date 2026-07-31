package de.fallstudie.minerva.backend.user.internal.persistence;

import de.fallstudie.minerva.backend.user.WorkspaceRoleName;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<UserModel, Long> {
	Optional<UserModel> findByUsername(String username);

	boolean existsByUsernameAndDeletedAtIsNull(String username);

	/// Prüft ob ein Benutzer in einem gültigen zustand existiert
	boolean existsByIdAndDeletedAtIsNullAndDeactivatedAtIsNull(long id);

	List<UserModel> findAllByDeletedAtIsNull();

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	List<UserModel> findAllByWorkspaceRole_NameAndDeletedAtIsNullAndDeactivatedAtIsNull(
			WorkspaceRoleName roleName);
}
