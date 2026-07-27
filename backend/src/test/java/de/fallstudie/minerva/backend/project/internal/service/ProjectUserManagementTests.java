package de.fallstudie.minerva.backend.project.internal.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.context.ApplicationEventPublisher;

import de.fallstudie.minerva.backend.common.DuplicateResourceException;
import de.fallstudie.minerva.backend.common.ResourceNotFoundException;
import de.fallstudie.minerva.backend.common.ReadOnlyException;
import de.fallstudie.minerva.backend.common.ValidationException;
import de.fallstudie.minerva.backend.project.internal.persistence.ProjectMemberModel;
import de.fallstudie.minerva.backend.project.internal.persistence.ProjectMemberRepository;
import de.fallstudie.minerva.backend.project.internal.persistence.ProjectRepository;
import de.fallstudie.minerva.backend.project.internal.persistence.ProjectRoleName;
import de.fallstudie.minerva.backend.project.internal.persistence.ProjectRoleRepository;
import de.fallstudie.minerva.backend.project.internal.web.AddProjectUserRequest;
import de.fallstudie.minerva.backend.project.internal.web.UpdateProjectUserRoleRequest;
import de.fallstudie.minerva.backend.project.ProjectEvent;
import de.fallstudie.minerva.backend.testsupport.TestProjects;
import de.fallstudie.minerva.backend.user.UserService;
import de.fallstudie.minerva.backend.user.UserDTO;
import de.fallstudie.minerva.backend.user.WorkspaceRoleName;
import de.fallstudie.minerva.backend.user.Identity;

class ProjectUserManagementTests {
	private UserService userService;
	private ProjectMemberRepository projectMemberRepository;
	private ProjectRepository projectRepository;
	private ProjectRoleRepository projectRoleRepository;
	private ApplicationEventPublisher eventPublisher;
	private ProjectService projectService;

	@BeforeEach
	void setUp() {
		userService = Mockito.mock(UserService.class);
		projectMemberRepository = Mockito.mock(ProjectMemberRepository.class);
		projectRepository = Mockito.mock(ProjectRepository.class);
		projectRoleRepository = Mockito.mock(ProjectRoleRepository.class);
		eventPublisher = Mockito.mock(ApplicationEventPublisher.class);
		projectService = new ProjectService(projectMemberRepository, projectRepository,
				projectRoleRepository, userService, eventPublisher);
	}

	@Test
	void getProjectUsersReturnsMembersAndTheirProjectRoles() {
		final var ownerMember = TestProjects.projectMember(TestProjects.PROJECT_ID,
				TestProjects.OWNER_USER_ID, TestProjects.OWNER_ROLE_ID);
		when(projectRepository.existsById(TestProjects.PROJECT_ID)).thenReturn(true);
		when(projectMemberRepository.findAllByProjectId(TestProjects.PROJECT_ID))
				.thenReturn(List.of(ownerMember));
		when(projectRoleRepository.findAllByProjectId(TestProjects.PROJECT_ID))
				.thenReturn(List.of(TestProjects.projectRole(TestProjects.OWNER_ROLE_ID,
						TestProjects.PROJECT_ID, ProjectRoleName.OWNER)));
		when(userService.findAll())
				.thenReturn(List.of(TestProjects.user(TestProjects.OWNER_USER_ID, "owner"),
						TestProjects.user(TestProjects.OUTSIDER_USER_ID, "outsider")));

		final var response = projectService.getProjectUsers(TestProjects.OWNER,
				TestProjects.PROJECT_ID);

		assertEquals(1, response.users().size());
		final var owner = response.users().getFirst();
		assertEquals(TestProjects.OWNER_USER_ID, owner.id());
		assertEquals(ProjectRoleName.OWNER, owner.projectRole());
	}

	@Test
	void addProjectUserSavesNewMemberWithRequestedRole() {
		final var role = TestProjects.projectRole(TestProjects.CONTRIBUTOR_ROLE_ID,
				TestProjects.PROJECT_ID, ProjectRoleName.CONTRIBUTOR);
		when(projectRepository.existsById(TestProjects.PROJECT_ID)).thenReturn(true);
		when(userService.existsById(TestProjects.CONTRIBUTOR_USER_ID)).thenReturn(true);
		when(projectMemberRepository.existsByProjectIdAndUserId(TestProjects.PROJECT_ID,
				TestProjects.CONTRIBUTOR_USER_ID)).thenReturn(false);
		when(projectRoleRepository.findByProjectIdAndName(TestProjects.PROJECT_ID,
				ProjectRoleName.CONTRIBUTOR)).thenReturn(Optional.of(role));

		projectService.addProjectUser(TestProjects.OWNER, TestProjects.PROJECT_ID,
				new AddProjectUserRequest(TestProjects.CONTRIBUTOR_USER_ID, "CONTRIBUTOR"));

		final var memberCaptor = ArgumentCaptor.forClass(ProjectMemberModel.class);
		verify(projectMemberRepository).save(memberCaptor.capture());
		final var savedMember = memberCaptor.getValue();
		assertEquals(TestProjects.PROJECT_ID, savedMember.getProjectId());
		assertEquals(TestProjects.CONTRIBUTOR_USER_ID, savedMember.getUserId());
		assertEquals(TestProjects.CONTRIBUTOR_ROLE_ID, savedMember.getRoleId());
		verify(eventPublisher).publishEvent(new ProjectEvent.UserAdded(TestProjects.OWNER_USER_ID,
				TestProjects.PROJECT_ID, TestProjects.CONTRIBUTOR_USER_ID, "CONTRIBUTOR"));
	}

	@Test
	void addProjectUserRejectsUnknownProject() {
		when(projectRepository.existsById(TestProjects.PROJECT_ID)).thenReturn(false);

		assertThrows(ResourceNotFoundException.class, () -> projectService.addProjectUser(
				TestProjects.OWNER, TestProjects.PROJECT_ID,
				new AddProjectUserRequest(TestProjects.CONTRIBUTOR_USER_ID, "CONTRIBUTOR")));

		verify(projectMemberRepository, never()).save(any());
	}

	@Test
	void addProjectUserRejectsArchivedProject() {
		when(projectRepository.existsById(TestProjects.PROJECT_ID)).thenReturn(true);
		when(projectRepository.existsByIdAndArchivedAtIsNotNull(TestProjects.PROJECT_ID))
				.thenReturn(true);

		assertThrows(ReadOnlyException.class, () -> projectService.addProjectUser(
				TestProjects.OWNER, TestProjects.PROJECT_ID,
				new AddProjectUserRequest(TestProjects.CONTRIBUTOR_USER_ID, "CONTRIBUTOR")));

		verify(projectMemberRepository, never()).save(any());
		verify(eventPublisher, never()).publishEvent(any());
	}

	@Test
	void addProjectUserRejectsUnknownUser() {
		when(projectRepository.existsById(TestProjects.PROJECT_ID)).thenReturn(true);
		when(userService.existsById(TestProjects.CONTRIBUTOR_USER_ID)).thenReturn(false);

		assertThrows(ResourceNotFoundException.class, () -> projectService.addProjectUser(
				TestProjects.OWNER, TestProjects.PROJECT_ID,
				new AddProjectUserRequest(TestProjects.CONTRIBUTOR_USER_ID, "CONTRIBUTOR")));

		verify(projectMemberRepository, never()).save(any());
	}

	@Test
	void addProjectUserRejectsExistingMember() {
		when(projectRepository.existsById(TestProjects.PROJECT_ID)).thenReturn(true);
		when(userService.existsById(TestProjects.CONTRIBUTOR_USER_ID)).thenReturn(true);
		when(projectMemberRepository.existsByProjectIdAndUserId(TestProjects.PROJECT_ID,
				TestProjects.CONTRIBUTOR_USER_ID)).thenReturn(true);

		assertThrows(DuplicateResourceException.class, () -> projectService.addProjectUser(
				TestProjects.OWNER, TestProjects.PROJECT_ID,
				new AddProjectUserRequest(TestProjects.CONTRIBUTOR_USER_ID, "CONTRIBUTOR")));

		verify(projectMemberRepository, never()).save(any());
	}

	@Test
	void addProjectUserRejectsInvalidRole() {
		when(projectRepository.existsById(TestProjects.PROJECT_ID)).thenReturn(true);

		assertThrows(ValidationException.class,
				() -> projectService.addProjectUser(TestProjects.OWNER, TestProjects.PROJECT_ID,
						new AddProjectUserRequest(TestProjects.CONTRIBUTOR_USER_ID, "ADMIN")));

		verify(projectMemberRepository, never()).save(any());
	}

	@Test
	void adminCanPromoteExistingMemberToOwner() {
		final var admin = new Identity(TestProjects.OUTSIDER_USER_ID);
		final var member = TestProjects.projectMember(TestProjects.PROJECT_ID,
				TestProjects.CONTRIBUTOR_USER_ID, TestProjects.CONTRIBUTOR_ROLE_ID);
		final var ownerRole = TestProjects.projectRole(TestProjects.OWNER_ROLE_ID,
				TestProjects.PROJECT_ID, ProjectRoleName.OWNER);
		final var contributorRole = TestProjects.projectRole(TestProjects.CONTRIBUTOR_ROLE_ID,
				TestProjects.PROJECT_ID, ProjectRoleName.CONTRIBUTOR);
		when(projectRepository.existsById(TestProjects.PROJECT_ID)).thenReturn(true);
		when(userService.findActiveById(TestProjects.OUTSIDER_USER_ID))
				.thenReturn(Optional.of(new UserDTO(TestProjects.OUTSIDER_USER_ID, "admin", "hash",
						WorkspaceRoleName.ADMIN, false)));
		when(projectMemberRepository.findByProjectIdAndUserId(TestProjects.PROJECT_ID,
				TestProjects.CONTRIBUTOR_USER_ID)).thenReturn(Optional.of(member));
		when(projectRoleRepository.findByProjectIdAndName(TestProjects.PROJECT_ID,
				ProjectRoleName.OWNER)).thenReturn(Optional.of(ownerRole));
		when(projectRoleRepository.findById(TestProjects.CONTRIBUTOR_ROLE_ID))
				.thenReturn(Optional.of(contributorRole));

		projectService.updateProjectUserRole(admin, TestProjects.PROJECT_ID,
				TestProjects.CONTRIBUTOR_USER_ID, new UpdateProjectUserRoleRequest("OWNER"));

		assertEquals(TestProjects.OWNER_ROLE_ID, member.getRoleId());
	}

	@Test
	void adminCannotAssignNonOwnerProjectRole() {
		final var admin = new Identity(TestProjects.OUTSIDER_USER_ID);
		when(projectRepository.existsById(TestProjects.PROJECT_ID)).thenReturn(true);
		when(userService.findActiveById(TestProjects.OUTSIDER_USER_ID))
				.thenReturn(Optional.of(new UserDTO(TestProjects.OUTSIDER_USER_ID, "admin", "hash",
						WorkspaceRoleName.ADMIN, false)));

		assertThrows(ValidationException.class,
				() -> projectService.updateProjectUserRole(admin, TestProjects.PROJECT_ID,
						TestProjects.CONTRIBUTOR_USER_ID,
						new UpdateProjectUserRoleRequest("VIEWER")));

		verify(projectMemberRepository, never()).save(any());
	}

	@Test
	void updateProjectUserRoleSavesChangedRole() {
		final var member = TestProjects.projectMember(TestProjects.PROJECT_ID,
				TestProjects.CONTRIBUTOR_USER_ID, TestProjects.VIEWER_ROLE_ID);
		final var contributorRole = TestProjects.projectRole(TestProjects.CONTRIBUTOR_ROLE_ID,
				TestProjects.PROJECT_ID, ProjectRoleName.CONTRIBUTOR);
		final var viewerRole = TestProjects.projectRole(TestProjects.VIEWER_ROLE_ID,
				TestProjects.PROJECT_ID, ProjectRoleName.VIEWER);
		when(projectRepository.existsById(TestProjects.PROJECT_ID)).thenReturn(true);
		when(projectMemberRepository.findByProjectIdAndUserId(TestProjects.PROJECT_ID,
				TestProjects.CONTRIBUTOR_USER_ID)).thenReturn(Optional.of(member));
		when(projectRoleRepository.findByProjectIdAndName(TestProjects.PROJECT_ID,
				ProjectRoleName.CONTRIBUTOR)).thenReturn(Optional.of(contributorRole));
		when(projectRoleRepository.findById(TestProjects.VIEWER_ROLE_ID))
				.thenReturn(Optional.of(viewerRole));

		projectService.updateProjectUserRole(TestProjects.OWNER, TestProjects.PROJECT_ID,
				TestProjects.CONTRIBUTOR_USER_ID, new UpdateProjectUserRoleRequest("CONTRIBUTOR"));

		final var memberCaptor = ArgumentCaptor.forClass(ProjectMemberModel.class);
		verify(projectMemberRepository).save(memberCaptor.capture());
		assertEquals(TestProjects.CONTRIBUTOR_ROLE_ID, memberCaptor.getValue().getRoleId());
		verify(eventPublisher).publishEvent(new ProjectEvent.UserRoleChanged(
				TestProjects.OWNER_USER_ID, TestProjects.PROJECT_ID,
				TestProjects.CONTRIBUTOR_USER_ID, "VIEWER", "CONTRIBUTOR"));
	}

	@Test
	void updateProjectUserRoleReturnsWithoutSavingWhenRoleDoesNotChange() {
		final var member = TestProjects.projectMember(TestProjects.PROJECT_ID,
				TestProjects.CONTRIBUTOR_USER_ID, TestProjects.CONTRIBUTOR_ROLE_ID);
		final var contributorRole = TestProjects.projectRole(TestProjects.CONTRIBUTOR_ROLE_ID,
				TestProjects.PROJECT_ID, ProjectRoleName.CONTRIBUTOR);
		when(projectRepository.existsById(TestProjects.PROJECT_ID)).thenReturn(true);
		when(projectMemberRepository.findByProjectIdAndUserId(TestProjects.PROJECT_ID,
				TestProjects.CONTRIBUTOR_USER_ID)).thenReturn(Optional.of(member));
		when(projectRoleRepository.findByProjectIdAndName(TestProjects.PROJECT_ID,
				ProjectRoleName.CONTRIBUTOR)).thenReturn(Optional.of(contributorRole));

		projectService.updateProjectUserRole(TestProjects.OWNER, TestProjects.PROJECT_ID,
				TestProjects.CONTRIBUTOR_USER_ID, new UpdateProjectUserRoleRequest("CONTRIBUTOR"));

		verify(projectMemberRepository, never()).save(any());
		verify(eventPublisher, never()).publishEvent(any());
	}

	@Test
	void updateProjectUserRoleRejectsSelfRoleUpdate() {
		when(projectRepository.existsById(TestProjects.PROJECT_ID)).thenReturn(true);

		assertThrows(ValidationException.class,
				() -> projectService.updateProjectUserRole(TestProjects.OWNER,
						TestProjects.PROJECT_ID, TestProjects.OWNER_USER_ID,
						new UpdateProjectUserRoleRequest("VIEWER")));

		verify(projectMemberRepository, never()).save(any());
	}

	@Test
	void updateProjectUserRoleRejectsUnknownMember() {
		when(projectRepository.existsById(TestProjects.PROJECT_ID)).thenReturn(true);
		when(projectMemberRepository.findByProjectIdAndUserId(TestProjects.PROJECT_ID,
				TestProjects.CONTRIBUTOR_USER_ID)).thenReturn(Optional.empty());

		assertThrows(ResourceNotFoundException.class,
				() -> projectService.updateProjectUserRole(TestProjects.OWNER,
						TestProjects.PROJECT_ID, TestProjects.CONTRIBUTOR_USER_ID,
						new UpdateProjectUserRoleRequest("VIEWER")));

		verify(projectMemberRepository, never()).save(any());
	}

	@Test
	void removeProjectUserDeletesMemberAndPublishesEvent() {
		final var member = TestProjects.projectMember(TestProjects.PROJECT_ID,
				TestProjects.CONTRIBUTOR_USER_ID, TestProjects.CONTRIBUTOR_ROLE_ID);
		when(projectRepository.existsById(TestProjects.PROJECT_ID)).thenReturn(true);
		when(projectMemberRepository.findByProjectIdAndUserId(TestProjects.PROJECT_ID,
				TestProjects.CONTRIBUTOR_USER_ID)).thenReturn(Optional.of(member));

		projectService.removeProjectUser(TestProjects.OWNER, TestProjects.PROJECT_ID,
				TestProjects.CONTRIBUTOR_USER_ID);

		verify(projectMemberRepository).delete(member);
		verify(eventPublisher).publishEvent(new ProjectEvent.UserRemoved(TestProjects.OWNER_USER_ID,
				TestProjects.PROJECT_ID, TestProjects.CONTRIBUTOR_USER_ID));
	}

	@Test
	void removeProjectUserRejectsSelfRemoval() {
		when(projectRepository.existsById(TestProjects.PROJECT_ID)).thenReturn(true);

		assertThrows(ValidationException.class,
				() -> projectService.removeProjectUser(TestProjects.OWNER, TestProjects.PROJECT_ID,
						TestProjects.OWNER_USER_ID));

		verify(projectMemberRepository, never()).delete(any());
	}

	@Test
	void removeProjectUserRejectsUnknownMember() {
		when(projectRepository.existsById(TestProjects.PROJECT_ID)).thenReturn(true);
		when(projectMemberRepository.findByProjectIdAndUserId(TestProjects.PROJECT_ID,
				TestProjects.CONTRIBUTOR_USER_ID)).thenReturn(Optional.empty());

		assertThrows(ResourceNotFoundException.class,
				() -> projectService.removeProjectUser(TestProjects.OWNER, TestProjects.PROJECT_ID,
						TestProjects.CONTRIBUTOR_USER_ID));

		verify(projectMemberRepository, never()).delete(any());
	}

	@Test
	void removeProjectUserRejectsUnknownProject() {
		when(projectRepository.existsById(TestProjects.PROJECT_ID)).thenReturn(false);

		assertThrows(ResourceNotFoundException.class,
				() -> projectService.removeProjectUser(TestProjects.OWNER, TestProjects.PROJECT_ID,
						TestProjects.CONTRIBUTOR_USER_ID));

		verify(projectMemberRepository, never()).delete(any());
	}
}
