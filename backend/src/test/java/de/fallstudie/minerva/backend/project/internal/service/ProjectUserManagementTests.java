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

import de.fallstudie.minerva.backend.common.DuplicateResourceException;
import de.fallstudie.minerva.backend.common.ResourceNotFoundException;
import de.fallstudie.minerva.backend.common.ValidationException;
import de.fallstudie.minerva.backend.project.internal.persistence.ProjectMemberModel;
import de.fallstudie.minerva.backend.project.internal.persistence.ProjectMemberRepository;
import de.fallstudie.minerva.backend.project.internal.persistence.ProjectRepository;
import de.fallstudie.minerva.backend.project.internal.persistence.ProjectRoleName;
import de.fallstudie.minerva.backend.project.internal.persistence.ProjectRoleRepository;
import de.fallstudie.minerva.backend.project.internal.web.AddProjectUserRequest;
import de.fallstudie.minerva.backend.project.internal.web.UpdateProjectUserRoleRequest;
import de.fallstudie.minerva.backend.ticket.TicketConfigurationService;
import de.fallstudie.minerva.backend.testsupport.TestProjects;
import de.fallstudie.minerva.backend.user.UserService;

class ProjectUserManagementTests {
	private UserService userService;
	private ProjectMemberRepository projectMemberRepository;
	private ProjectRepository projectRepository;
	private ProjectRoleRepository projectRoleRepository;
	private TicketConfigurationService ticketConfigurationService;
	private ProjectService projectService;

	@BeforeEach
	void setUp() {
		userService = Mockito.mock(UserService.class);
		projectMemberRepository = Mockito.mock(ProjectMemberRepository.class);
		projectRepository = Mockito.mock(ProjectRepository.class);
		projectRoleRepository = Mockito.mock(ProjectRoleRepository.class);
		ticketConfigurationService = Mockito.mock(TicketConfigurationService.class);
		projectService = new ProjectService(projectMemberRepository, projectRepository,
				projectRoleRepository, ticketConfigurationService, userService);
	}

	@Test
	void getProjectUsersMarksMembersAndTheirProjectRoles() {
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

		assertEquals(2, response.users().size());
		final var owner = response.users().get(0);
		final var outsider = response.users().get(1);
		assertEquals(TestProjects.OWNER_USER_ID, owner.id());
		assertEquals(ProjectRoleName.OWNER, owner.projectRole());
		assertTrue(owner.member());
		assertEquals(TestProjects.OUTSIDER_USER_ID, outsider.id());
		assertNull(outsider.projectRole());
		assertFalse(outsider.member());
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
	void updateProjectUserRoleSavesChangedRole() {
		final var member = TestProjects.projectMember(TestProjects.PROJECT_ID,
				TestProjects.CONTRIBUTOR_USER_ID, TestProjects.VIEWER_ROLE_ID);
		final var contributorRole = TestProjects.projectRole(TestProjects.CONTRIBUTOR_ROLE_ID,
				TestProjects.PROJECT_ID, ProjectRoleName.CONTRIBUTOR);
		when(projectRepository.existsById(TestProjects.PROJECT_ID)).thenReturn(true);
		when(projectMemberRepository.findByProjectIdAndUserId(TestProjects.PROJECT_ID,
				TestProjects.CONTRIBUTOR_USER_ID)).thenReturn(Optional.of(member));
		when(projectRoleRepository.findByProjectIdAndName(TestProjects.PROJECT_ID,
				ProjectRoleName.CONTRIBUTOR)).thenReturn(Optional.of(contributorRole));

		projectService.updateProjectUserRole(TestProjects.OWNER, TestProjects.PROJECT_ID,
				TestProjects.CONTRIBUTOR_USER_ID, new UpdateProjectUserRoleRequest("CONTRIBUTOR"));

		final var memberCaptor = ArgumentCaptor.forClass(ProjectMemberModel.class);
		verify(projectMemberRepository).save(memberCaptor.capture());
		assertEquals(TestProjects.CONTRIBUTOR_ROLE_ID, memberCaptor.getValue().getRoleId());
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
}
