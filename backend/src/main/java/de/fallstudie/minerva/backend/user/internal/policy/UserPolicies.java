package de.fallstudie.minerva.backend.user.internal.policy;

import de.fallstudie.minerva.backend.user.Identity;
import de.fallstudie.minerva.backend.user.UserService;
import de.fallstudie.minerva.backend.user.WorkspaceRoleName;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component("userPolicies")
@RequiredArgsConstructor
public class UserPolicies {
	private final UserService userService;

	public boolean isAdmin(Identity identity) {
		assert identity != null;

		// auth erstellt identity nur mit gültigem user, daher wäre dies ein ungültiger
		// Zustand
		final var user = userService.findById(identity.userId())
				.orElseThrow(IllegalStateException::new);
		return user.workspaceRole().equals(WorkspaceRoleName.ADMIN);
	}
}
