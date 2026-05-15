package de.fallstudie.minerva.backend.authorization;

import de.fallstudie.minerva.backend.authorization.internal.WorkspaceRoleRepository;
import de.fallstudie.minerva.backend.authorization.internal.exception.InvalidUserException;
import de.fallstudie.minerva.backend.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WorkspaceAuthorizationService {
	private final WorkspaceRoleRepository workspaceRoleRepository;
	private final UserRepository userRepository;

	public void isWorkspaceAdmin(long userid) throws AuthorizationException {
		final var user = userRepository.findById(userid).orElseThrow(InvalidUserException::new);
		if (user.getWorkspaceRole().getName() != WorkspaceRoleName.ADMIN) {
			throw new InvalidUserException();
		}
	}
}
