package de.fallstudie.minerva.backend.project;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mockito;

import de.fallstudie.minerva.backend.project.internal.persistence.ProjectMemberRepository;
import de.fallstudie.minerva.backend.project.internal.persistence.ProjectRoleName;
import de.fallstudie.minerva.backend.project.internal.persistence.ProjectRoleRepository;
import de.fallstudie.minerva.backend.project.internal.persistence.ProjectRepository;
import de.fallstudie.minerva.backend.testsupport.TestProjects;
import de.fallstudie.minerva.backend.user.Identity;
import de.fallstudie.minerva.backend.user.UserDTO;
import de.fallstudie.minerva.backend.user.UserService;
import de.fallstudie.minerva.backend.user.WorkspaceRoleName;

class ProjectPoliciesTests {
	private ProjectMemberRepository projectMemberRepository;
	private ProjectRoleRepository projectRoleRepository;
	private ProjectPolicies projectPolicies;
	private UserService userService;

	@BeforeEach
	void setUp() {
		projectMemberRepository = Mockito.mock(ProjectMemberRepository.class);
		projectRoleRepository = Mockito.mock(ProjectRoleRepository.class);
		userService = Mockito.mock(UserService.class);
		projectPolicies = new ProjectPolicies(projectMemberRepository, projectRoleRepository,
				Mockito.mock(ProjectRepository.class), userService);
	}

	@Test
	void canViewProjectAllowsOnlyMembers() {
		when(projectMemberRepository.existsByProjectIdAndUserId(TestProjects.PROJECT_ID,
				TestProjects.OWNER_USER_ID)).thenReturn(true);
		when(projectMemberRepository.existsByProjectIdAndUserId(TestProjects.PROJECT_ID,
				TestProjects.OUTSIDER_USER_ID)).thenReturn(false);

		assertTrue(projectPolicies.canViewProject(TestProjects.OWNER, TestProjects.PROJECT_ID));
		assertFalse(projectPolicies.canViewProject(new Identity(TestProjects.OUTSIDER_USER_ID),
				TestProjects.PROJECT_ID));
	}

	@Test
	void canViewProjectAllowsAdminWithoutMembership() {
		final var admin = new Identity(TestProjects.OUTSIDER_USER_ID);
		when(userService.findActiveById(TestProjects.OUTSIDER_USER_ID))
				.thenReturn(Optional.of(new UserDTO(TestProjects.OUTSIDER_USER_ID, "admin", "hash",
						WorkspaceRoleName.ADMIN, false)));

		assertTrue(projectPolicies.canViewProject(admin, TestProjects.PROJECT_ID));
	}

	@Test
	void canUpdateProjectUserRoleAllowsAdminWithoutMembership() {
		final var admin = new Identity(TestProjects.OUTSIDER_USER_ID);
		when(userService.findActiveById(TestProjects.OUTSIDER_USER_ID))
				.thenReturn(Optional.of(new UserDTO(TestProjects.OUTSIDER_USER_ID, "admin", "hash",
						WorkspaceRoleName.ADMIN, false)));

		assertTrue(projectPolicies.canUpdateProjectUserRole(admin, TestProjects.PROJECT_ID));
	}

	@Test
	void adminCanManageProjectAndTicketsWithoutMembership() {
		final var admin = new Identity(TestProjects.OUTSIDER_USER_ID);
		when(userService.findActiveById(TestProjects.OUTSIDER_USER_ID))
				.thenReturn(Optional.of(new UserDTO(TestProjects.OUTSIDER_USER_ID, "admin", "hash",
						WorkspaceRoleName.ADMIN, false)));

		assertTrue(projectPolicies.canManageProjectUsers(admin, TestProjects.PROJECT_ID));
		assertTrue(projectPolicies.canModifyTickets(admin, TestProjects.PROJECT_ID));
		assertFalse(projectPolicies.canBeAssigned(TestProjects.PROJECT_ID,
				TestProjects.OUTSIDER_USER_ID));
	}

	@ParameterizedTest
	@CsvSource({"OWNER,true", "CONTRIBUTOR,false", "VIEWER,false"})
	void canManageProjectUsersAllowsExpectedRoles(ProjectRoleName roleName, boolean expected) {
		assertEquals(expected, canManageUsersWithRole(roleName));
	}

	@Test
	void canManageProjectUsersRejectsNonMembers() {
		when(projectMemberRepository.existsByProjectIdAndUserId(TestProjects.PROJECT_ID,
				TestProjects.OWNER_USER_ID)).thenReturn(false);

		assertFalse(
				projectPolicies.canManageProjectUsers(TestProjects.OWNER, TestProjects.PROJECT_ID));
	}

	@ParameterizedTest
	@CsvSource({"OWNER,true", "CONTRIBUTOR,true", "VIEWER,false"})
	void canModifyTicketsAllowsExpectedRoles(ProjectRoleName roleName, boolean expected) {
		assertEquals(expected, canModifyTicketsWithRole(roleName));
	}

	@Test
	void canModifyTicketsRejectsNonMembers() {
		when(projectMemberRepository.existsByProjectIdAndUserId(TestProjects.PROJECT_ID,
				TestProjects.OWNER_USER_ID)).thenReturn(false);

		assertFalse(projectPolicies.canModifyTickets(TestProjects.OWNER, TestProjects.PROJECT_ID));
	}

	private boolean canManageUsersWithRole(ProjectRoleName roleName) {
		stubMembershipRole(roleName);
		return projectPolicies.canManageProjectUsers(TestProjects.OWNER, TestProjects.PROJECT_ID);
	}

	private boolean canModifyTicketsWithRole(ProjectRoleName roleName) {
		stubMembershipRole(roleName);
		return projectPolicies.canModifyTickets(TestProjects.OWNER, TestProjects.PROJECT_ID);
	}

	private void stubMembershipRole(ProjectRoleName roleName) {
		final var member = TestProjects.projectMember(TestProjects.PROJECT_ID,
				TestProjects.OWNER_USER_ID, TestProjects.OWNER_ROLE_ID);
		final var role = TestProjects.projectRole(TestProjects.OWNER_ROLE_ID,
				TestProjects.PROJECT_ID, roleName);
		when(projectMemberRepository.existsByProjectIdAndUserId(TestProjects.PROJECT_ID,
				TestProjects.OWNER_USER_ID)).thenReturn(true);
		when(projectMemberRepository.findByProjectIdAndUserId(TestProjects.PROJECT_ID,
				TestProjects.OWNER_USER_ID)).thenReturn(Optional.of(member));
		when(projectRoleRepository.findById(TestProjects.OWNER_ROLE_ID))
				.thenReturn(Optional.of(role));
	}
}
