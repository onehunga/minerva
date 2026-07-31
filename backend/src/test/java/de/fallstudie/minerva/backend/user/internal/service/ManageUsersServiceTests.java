package de.fallstudie.minerva.backend.user.internal.service;

import de.fallstudie.minerva.backend.common.DuplicateResourceException;
import de.fallstudie.minerva.backend.common.ResourceNotFoundException;
import de.fallstudie.minerva.backend.common.ValidationException;
import de.fallstudie.minerva.backend.user.WorkspaceRoleName;
import de.fallstudie.minerva.backend.user.WorkspaceRoleService;
import de.fallstudie.minerva.backend.user.UserEvent;
import de.fallstudie.minerva.backend.user.internal.persistence.UserModel;
import de.fallstudie.minerva.backend.user.internal.persistence.UserRepository;
import de.fallstudie.minerva.backend.user.internal.persistence.WorkspaceRoleModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ManageUsersServiceTests {
	private static final long USER_ID = 42L;
	private static final long ACTOR_USER_ID = 1L;

	private PasswordEncoder passwordEncoder;
	private WorkspaceRoleService workspaceRoleService;
	private UserRepository userRepository;
	private ApplicationEventPublisher eventPublisher;
	private ManageUsersService manageUsersService;

	@BeforeEach
	void setUp() {
		passwordEncoder = mock(PasswordEncoder.class);
		workspaceRoleService = mock(WorkspaceRoleService.class);
		userRepository = mock(UserRepository.class);
		eventPublisher = mock(ApplicationEventPublisher.class);
		manageUsersService = new ManageUsersService(passwordEncoder, workspaceRoleService,
				userRepository, eventPublisher);
	}

	@Test
	void createUserSavesValidUser() {
		final var userRole = mock(WorkspaceRoleModel.class);

		when(userRepository.existsByUsernameAndDeletedAtIsNull("valid-user")).thenReturn(false);
		when(workspaceRoleService.find(WorkspaceRoleName.USER)).thenReturn(Optional.of(userRole));
		when(passwordEncoder.encode("password1")).thenReturn("encoded-password");
		final var savedUserWithId = mock(UserModel.class);
		when(savedUserWithId.getId()).thenReturn(USER_ID);
		when(userRepository.save(any(UserModel.class))).thenReturn(savedUserWithId);

		manageUsersService.createUser(ACTOR_USER_ID, " valid-user ", "password1", "USER");

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
		when(userRepository.existsByUsernameAndDeletedAtIsNull("valid-user")).thenReturn(true);

		assertThrows(DuplicateResourceException.class, () -> manageUsersService
				.createUser(ACTOR_USER_ID, "valid-user", "password1", "USER"));

		verify(userRepository, never()).save(any());
		verify(userRepository, never()).flush();
	}

	@Test
	void updateUsernameSavesValidUsername() {
		final var user = createUser("jane", WorkspaceRoleName.USER);

		when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
		when(userRepository.existsByUsernameAndDeletedAtIsNull("new-jane")).thenReturn(false);

		manageUsersService.updateUsername(ACTOR_USER_ID, USER_ID, " new-jane ");

		assertEquals("new-jane", user.getUsername());
		verify(userRepository).save(user);
		verify(userRepository).flush();
	}

	@Test
	void updateUsernameRejectsInvalidUsername() {
		assertThrows(ValidationException.class,
				() -> manageUsersService.updateUsername(ACTOR_USER_ID, USER_ID, "ab"));

		verify(userRepository, never()).findById(anyLong());
		verify(userRepository, never()).save(any());
		verify(userRepository, never()).flush();
	}

	@Test
	void updateUsernameRejectsDuplicateUsername() {
		final var user = createUser("jane", WorkspaceRoleName.USER);

		when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
		when(userRepository.existsByUsernameAndDeletedAtIsNull("max")).thenReturn(true);

		assertThrows(DuplicateResourceException.class,
				() -> manageUsersService.updateUsername(ACTOR_USER_ID, USER_ID, "max"));

		assertEquals("jane", user.getUsername());
		verify(userRepository, never()).save(any());
		verify(userRepository, never()).flush();
	}

	@Test
	void updateUsernameRejectsUnknownUser() {
		when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

		assertThrows(ResourceNotFoundException.class,
				() -> manageUsersService.updateUsername(ACTOR_USER_ID, USER_ID, "new-jane"));

		verify(userRepository, never()).existsByUsernameAndDeletedAtIsNull(anyString());
		verify(userRepository, never()).save(any());
		verify(userRepository, never()).flush();
	}

	@Test
	void updatePasswordSavesEncodedPassword() {
		final var user = createUser("jane", WorkspaceRoleName.USER);

		when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
		when(passwordEncoder.encode("password2")).thenReturn("encoded-password-2");

		manageUsersService.updatePassword(ACTOR_USER_ID, USER_ID, "password2");

		assertEquals("encoded-password-2", user.getPassword());
		verify(userRepository).save(user);
		verify(userRepository).flush();
	}

	@Test
	void updatePasswordRejectsMissingPassword() {
		assertThrows(ValidationException.class,
				() -> manageUsersService.updatePassword(ACTOR_USER_ID, USER_ID, null));
		assertThrows(ValidationException.class,
				() -> manageUsersService.updatePassword(ACTOR_USER_ID, USER_ID, "  "));

		verify(userRepository, never()).findById(anyLong());
		verify(userRepository, never()).save(any());
		verify(userRepository, never()).flush();
	}

	@Test
	void updatePasswordRejectsShortPassword() {
		assertThrows(ValidationException.class,
				() -> manageUsersService.updatePassword(ACTOR_USER_ID, USER_ID, "short"));

		verify(userRepository, never()).findById(anyLong());
		verify(userRepository, never()).save(any());
		verify(userRepository, never()).flush();
	}

	@Test
	void updatePasswordRejectsUnknownUser() {
		when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

		assertThrows(ResourceNotFoundException.class,
				() -> manageUsersService.updatePassword(ACTOR_USER_ID, USER_ID, "password2"));

		verify(passwordEncoder, never()).encode(anyString());
		verify(userRepository, never()).save(any());
		verify(userRepository, never()).flush();
	}

	@Test
	void updateUserRolePromotesUserToAdmin() {
		final var user = createUser("jane", WorkspaceRoleName.USER);
		final var adminRole = createRole(WorkspaceRoleName.ADMIN);

		when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
		when(workspaceRoleService.find(WorkspaceRoleName.ADMIN)).thenReturn(Optional.of(adminRole));

		manageUsersService.updateUserRole(ACTOR_USER_ID, USER_ID, "ADMIN");

		assertEquals(adminRole, user.getWorkspaceRole());
		verify(userRepository).save(user);
		verify(userRepository).flush();
		verify(userRepository, never())
				.findAllByWorkspaceRole_NameAndDeletedAtIsNullAndDeactivatedAtIsNull(any());
	}

	@Test
	void updateUserRoleDemotesAdminWhenAnotherAdminRemains() {
		final var user = createUser("jane", WorkspaceRoleName.ADMIN);
		final var otherAdmin = createUser("other-admin", WorkspaceRoleName.ADMIN);
		final var userRole = createRole(WorkspaceRoleName.USER);

		when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
		when(userRepository.findAllByWorkspaceRole_NameAndDeletedAtIsNullAndDeactivatedAtIsNull(
				WorkspaceRoleName.ADMIN)).thenReturn(List.of(user, otherAdmin));
		when(workspaceRoleService.find(WorkspaceRoleName.USER)).thenReturn(Optional.of(userRole));

		manageUsersService.updateUserRole(ACTOR_USER_ID, USER_ID, "USER");

		assertEquals(userRole, user.getWorkspaceRole());
		verify(userRepository).save(user);
		verify(userRepository).flush();
	}

	@Test
	void updateUserRoleRejectsLastAdminDemotion() {
		final var user = createUser("jane", WorkspaceRoleName.ADMIN);

		when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
		when(userRepository.findAllByWorkspaceRole_NameAndDeletedAtIsNullAndDeactivatedAtIsNull(
				WorkspaceRoleName.ADMIN)).thenReturn(List.of(user));

		assertThrows(ValidationException.class,
				() -> manageUsersService.updateUserRole(ACTOR_USER_ID, USER_ID, "USER"));

		verify(workspaceRoleService, never()).find(any());
		verify(userRepository, never()).save(any());
		verify(userRepository, never()).flush();
	}

	@Test
	void updateUserRoleAllowsInitialAdminDemotionWhenAnotherAdminRemains() {
		final var user = createUser("admin", WorkspaceRoleName.ADMIN);
		final var otherAdmin = createUser("other-admin", WorkspaceRoleName.ADMIN);
		final var userRole = createRole(WorkspaceRoleName.USER);

		when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
		when(userRepository.findAllByWorkspaceRole_NameAndDeletedAtIsNullAndDeactivatedAtIsNull(
				WorkspaceRoleName.ADMIN)).thenReturn(List.of(user, otherAdmin));
		when(workspaceRoleService.find(WorkspaceRoleName.USER)).thenReturn(Optional.of(userRole));

		manageUsersService.updateUserRole(ACTOR_USER_ID, USER_ID, "USER");

		assertEquals(userRole, user.getWorkspaceRole());
	}

	@Test
	void updateUserRoleAllowsUnchangedRole() {
		final var user = createUser("admin", WorkspaceRoleName.ADMIN);

		when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));

		manageUsersService.updateUserRole(ACTOR_USER_ID, USER_ID, "ADMIN");

		verify(userRepository, never())
				.findAllByWorkspaceRole_NameAndDeletedAtIsNullAndDeactivatedAtIsNull(any());
		verify(workspaceRoleService, never()).find(any());
		verify(userRepository, never()).save(any());
		verify(userRepository, never()).flush();
	}

	@Test
	void updateUserRoleRejectsInvalidRole() {
		assertThrows(ValidationException.class,
				() -> manageUsersService.updateUserRole(ACTOR_USER_ID, USER_ID, "SUPERADMIN"));

		verify(userRepository, never()).findById(anyLong());
		verify(userRepository, never()).save(any());
		verify(userRepository, never()).flush();
	}

	@Test
	void updateUserRoleRejectsUnknownUser() {
		when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

		assertThrows(ResourceNotFoundException.class,
				() -> manageUsersService.updateUserRole(ACTOR_USER_ID, USER_ID, "USER"));

		verify(workspaceRoleService, never()).find(any());
		verify(userRepository, never()).save(any());
		verify(userRepository, never()).flush();
	}

	@Test
	void deleteUserAnonymizesUserAndPublishesEvent() {
		final var user = createUser("jane", WorkspaceRoleName.USER);
		when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));

		manageUsersService.deleteUser(1L, USER_ID);

		assertNull(user.getUsername());
		assertEquals("deleted", user.getPassword());
		assertNotNull(user.getDeletedAt());
		verify(userRepository).save(user);
		verify(userRepository).flush();
		verify(eventPublisher).publishEvent(new UserEvent.Deleted(1L, USER_ID, "jane"));
	}

	@Test
	void deleteUserRejectsAdmin() {
		final var user = createUser("admin", WorkspaceRoleName.ADMIN);
		when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));

		assertThrows(ValidationException.class, () -> manageUsersService.deleteUser(1L, USER_ID));

		verify(userRepository, never()).save(any());
		verify(eventPublisher, never()).publishEvent(any());
	}

	@Test
	void deleteUserRejectsUnknownUser() {
		when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

		assertThrows(ResourceNotFoundException.class,
				() -> manageUsersService.deleteUser(1L, USER_ID));

		verify(eventPublisher, never()).publishEvent(any());
		verify(userRepository, never()).save(any());
		verify(userRepository, never()).flush();
	}

	@Test
	void deactivateUserSetsStateAndPublishesEvent() {
		final var user = createUser("jane", WorkspaceRoleName.USER);
		when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));

		manageUsersService.deactivateUser(ACTOR_USER_ID, USER_ID);

		assertNotNull(user.getDeactivatedAt());
		verify(userRepository).save(user);
		verify(userRepository).flush();
		verify(eventPublisher).publishEvent(new UserEvent.Deactivated(ACTOR_USER_ID, USER_ID));
	}

	@Test
	void deactivateUserRejectsSelfDeactivation() {
		assertThrows(ValidationException.class,
				() -> manageUsersService.deactivateUser(USER_ID, USER_ID));

		verify(userRepository, never()).findById(anyLong());
		verify(eventPublisher, never()).publishEvent(any());
	}

	@Test
	void reactivateUserClearsStateAndPublishesEvent() {
		final var user = createUser("jane", WorkspaceRoleName.USER);
		user.setDeactivatedAt(java.time.Instant.now());
		when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));

		manageUsersService.reactivateUser(ACTOR_USER_ID, USER_ID);

		assertNull(user.getDeactivatedAt());
		verify(userRepository).save(user);
		verify(eventPublisher).publishEvent(new UserEvent.Reactivated(ACTOR_USER_ID, USER_ID));
	}

	private void assertInvalidUser(String username, String password, String role) {
		assertThrows(ValidationException.class,
				() -> manageUsersService.createUser(ACTOR_USER_ID, username, password, role));

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
