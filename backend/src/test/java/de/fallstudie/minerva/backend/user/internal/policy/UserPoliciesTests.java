package de.fallstudie.minerva.backend.user.internal.policy;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import de.fallstudie.minerva.backend.user.Identity;
import de.fallstudie.minerva.backend.user.UserDTO;
import de.fallstudie.minerva.backend.user.UserService;
import de.fallstudie.minerva.backend.user.WorkspaceRoleName;

class UserPoliciesTests {
	private static final long USER_ID = 1L;
	private static final long OTHER_USER_ID = 2L;

	private UserService userService;
	private UserPolicies userPolicies;

	@BeforeEach
	void setUp() {
		userService = mock(UserService.class);
		userPolicies = new UserPolicies(userService);
	}

	@Test
	void isAdminOrSelfAllowsSelfAndAdminsOnly() {
		final var user = new Identity(USER_ID);
		final var admin = new Identity(OTHER_USER_ID);

		when(userService.findById(OTHER_USER_ID)).thenReturn(Optional
				.of(new UserDTO(OTHER_USER_ID, "admin", "hash", WorkspaceRoleName.ADMIN, false)));
		when(userService.findById(3L)).thenReturn(
				Optional.of(new UserDTO(3L, "other", "hash", WorkspaceRoleName.USER, false)));

		assertTrue(userPolicies.isAdminOrSelf(user, USER_ID));
		assertTrue(userPolicies.isAdminOrSelf(admin, USER_ID));
		assertFalse(userPolicies.isAdminOrSelf(new Identity(3L), USER_ID));
	}
}
