package de.fallstudie.minerva.backend.user.internal.service;

import de.fallstudie.minerva.backend.auth.RefreshTokenRepository;
import de.fallstudie.minerva.backend.authorization.WorkspaceRoleModel;
import de.fallstudie.minerva.backend.authorization.WorkspaceRoleName;
import de.fallstudie.minerva.backend.authorization.WorkspaceRoleService;
import de.fallstudie.minerva.backend.common.DuplicateResourceException;
import de.fallstudie.minerva.backend.common.ResourceNotFoundException;
import de.fallstudie.minerva.backend.common.ValidationException;
import de.fallstudie.minerva.backend.user.UserModel;
import de.fallstudie.minerva.backend.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ManageUsersServiceTests {
	private PasswordEncoder passwordEncoder;
	private WorkspaceRoleService workspaceRoleService;
	private UserRepository userRepository;
	private RefreshTokenRepository refreshTokenRepository;
	private ManageUsersService manageUsersService;

	@BeforeEach
	void setUp() {
		passwordEncoder = mock(PasswordEncoder.class);
		workspaceRoleService = mock(WorkspaceRoleService.class);
		userRepository = mock(UserRepository.class);
		refreshTokenRepository = mock(RefreshTokenRepository.class);
		manageUsersService = new ManageUsersService(passwordEncoder, workspaceRoleService,
				userRepository, refreshTokenRepository);
	}

	@Test
	void createUserSavesValidUser() {
		final var userRole = mock(WorkspaceRoleModel.class);

		when(userRepository.existsByUsername("valid-user")).thenReturn(false);
		when(workspaceRoleService.find(WorkspaceRoleName.USER)).thenReturn(Optional.of(userRole));
		when(passwordEncoder.encode("password1")).thenReturn("encoded-password");

		manageUsersService.createUser(" valid-user ", "password1", "USER");

		ArgumentCaptor<UserModel> userCaptor = ArgumentCaptor.forClass(UserModel.class);
		verify(userRepository).save(userCaptor.capture());
		verify(userRepository).flush();

		UserModel savedUser = userCaptor.getValue();
		assertEquals("valid-user", savedUser.getUsername());
		assertEquals("encoded-password", savedUser.getPassword());
		assertEquals(userRole, savedUser.getWorkspaceRole());
	}

	@Test
	void createUserRejectsMissingUsername() {
		assertInvalidUser(null, "password1", "USER");
		assertInvalidUser("  ", "password1", "USER");
	}

	@Test
	void createUserRejectsInvalidUsernameLength() {
		assertInvalidUser("ab", "password1", "USER");
		assertInvalidUser("a".repeat(51), "password1", "USER");
	}

	@Test
	void createUserRejectsInvalidUsernameCharacters() {
		assertInvalidUser("invalid user", "password1", "USER");
		assertInvalidUser("invalid@example", "password1", "USER");
	}

	@Test
	void createUserRejectsMissingPassword() {
		assertInvalidUser("valid-user", null, "USER");
		assertInvalidUser("valid-user", "  ", "USER");
	}

	@Test
	void createUserRejectsShortPassword() {
		assertInvalidUser("valid-user", "short", "USER");
	}

	@Test
	void createUserRejectsMissingRole() {
		assertInvalidUser("valid-user", "password1", null);
		assertInvalidUser("valid-user", "password1", "  ");
	}

	@Test
	void createUserRejectsInvalidRole() {
		assertInvalidUser("valid-user", "password1", "SUPERADMIN");
		assertInvalidUser("valid-user", "password1", "user");
	}

	@Test
	void createUserRejectsDuplicateUsername() {
		when(userRepository.existsByUsername("valid-user")).thenReturn(true);

		assertThrows(DuplicateResourceException.class,
				() -> manageUsersService.createUser("valid-user", "password1", "USER"));

		verify(userRepository, never()).save(any());
		verify(userRepository, never()).flush();
	}

	@Test
	void updateUserRolePromotesUserToAdmin() {
		final var user = createUser("jane", WorkspaceRoleName.USER);
		final var adminRole = createRole(WorkspaceRoleName.ADMIN);

		when(userRepository.findById(42L)).thenReturn(Optional.of(user));
		when(workspaceRoleService.find(WorkspaceRoleName.ADMIN)).thenReturn(Optional.of(adminRole));

		manageUsersService.updateUserRole(42L, "ADMIN");

		assertEquals(adminRole, user.getWorkspaceRole());
		verify(userRepository).save(user);
		verify(userRepository).flush();
		verify(userRepository, never()).countByWorkspaceRole_Name(any());
	}

	@Test
	void updateUserRoleDemotesAdminWhenAnotherAdminRemains() {
		final var user = createUser("jane", WorkspaceRoleName.ADMIN);
		final var userRole = createRole(WorkspaceRoleName.USER);

		when(userRepository.findById(42L)).thenReturn(Optional.of(user));
		when(userRepository.countByWorkspaceRole_Name(WorkspaceRoleName.ADMIN)).thenReturn(2L);
		when(workspaceRoleService.find(WorkspaceRoleName.USER)).thenReturn(Optional.of(userRole));

		manageUsersService.updateUserRole(42L, "USER");

		assertEquals(userRole, user.getWorkspaceRole());
		verify(userRepository).save(user);
		verify(userRepository).flush();
	}

	@Test
	void updateUserRoleRejectsLastAdminDemotion() {
		final var user = createUser("jane", WorkspaceRoleName.ADMIN);

		when(userRepository.findById(42L)).thenReturn(Optional.of(user));
		when(userRepository.countByWorkspaceRole_Name(WorkspaceRoleName.ADMIN)).thenReturn(1L);

		assertThrows(ValidationException.class,
				() -> manageUsersService.updateUserRole(42L, "USER"));

		verify(workspaceRoleService, never()).find(any());
		verify(userRepository, never()).save(any());
		verify(userRepository, never()).flush();
	}

	@Test
	void updateUserRoleRejectsInitialAdminDemotion() {
		final var user = createUser("admin", WorkspaceRoleName.ADMIN);

		when(userRepository.findById(42L)).thenReturn(Optional.of(user));

		assertThrows(ValidationException.class,
				() -> manageUsersService.updateUserRole(42L, "USER"));

		verify(userRepository, never()).countByWorkspaceRole_Name(any());
		verify(workspaceRoleService, never()).find(any());
		verify(userRepository, never()).save(any());
		verify(userRepository, never()).flush();
	}

	@Test
	void updateUserRoleAllowsUnchangedRole() {
		final var user = createUser("admin", WorkspaceRoleName.ADMIN);

		when(userRepository.findById(42L)).thenReturn(Optional.of(user));

		manageUsersService.updateUserRole(42L, "ADMIN");

		verify(userRepository, never()).countByWorkspaceRole_Name(any());
		verify(workspaceRoleService, never()).find(any());
		verify(userRepository, never()).save(any());
		verify(userRepository, never()).flush();
	}

	@Test
	void updateUserRoleRejectsInvalidRole() {
		assertThrows(ValidationException.class,
				() -> manageUsersService.updateUserRole(42L, "SUPERADMIN"));

		verify(userRepository, never()).findById(anyLong());
		verify(userRepository, never()).save(any());
		verify(userRepository, never()).flush();
	}

	@Test
	void updateUserRoleRejectsUnknownUser() {
		when(userRepository.findById(42L)).thenReturn(Optional.empty());

		assertThrows(ResourceNotFoundException.class,
				() -> manageUsersService.updateUserRole(42L, "USER"));

		verify(workspaceRoleService, never()).find(any());
		verify(userRepository, never()).save(any());
		verify(userRepository, never()).flush();
	}

	@Test
	void deleteUserDeletesRefreshTokensAndUser() {
		final var user = mock(UserModel.class);

		when(userRepository.findById(42L)).thenReturn(Optional.of(user));

		manageUsersService.deleteUser(42L);

		verify(refreshTokenRepository).deleteByUserId(42L);
		verify(userRepository).delete(user);
		verify(userRepository).flush();
	}

	@Test
	void deleteUserRejectsUnknownUser() {
		when(userRepository.findById(42L)).thenReturn(Optional.empty());

		assertThrows(ResourceNotFoundException.class, () -> manageUsersService.deleteUser(42L));

		verify(refreshTokenRepository, never()).deleteByUserId(anyLong());
		verify(userRepository, never()).delete(any());
		verify(userRepository, never()).flush();
	}

	private void assertInvalidUser(String username, String password, String role) {
		assertThrows(ValidationException.class,
				() -> manageUsersService.createUser(username, password, role));

		verify(userRepository, never()).save(any());
		verify(userRepository, never()).flush();
	}

	private UserModel createUser(String username, WorkspaceRoleName roleName) {
		final var user = new UserModel();
		user.setUsername(username);
		user.setWorkspaceRole(createRole(roleName));
		return user;
	}

	private WorkspaceRoleModel createRole(WorkspaceRoleName roleName) {
		final var role = mock(WorkspaceRoleModel.class);
		when(role.getName()).thenReturn(roleName);
		return role;
	}
}
