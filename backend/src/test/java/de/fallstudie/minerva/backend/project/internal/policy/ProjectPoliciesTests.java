package de.fallstudie.minerva.backend.project.internal.policy;

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
import de.fallstudie.minerva.backend.testsupport.TestProjects;
import de.fallstudie.minerva.backend.user.Identity;

class ProjectPoliciesTests {
	private ProjectMemberRepository projectMemberRepository;
	private ProjectRoleRepository projectRoleRepository;
	private ProjectPolicies projectPolicies;

	@BeforeEach
	void setUp() {
		projectMemberRepository = Mockito.mock(ProjectMemberRepository.class);
		projectRoleRepository = Mockito.mock(ProjectRoleRepository.class);
		projectPolicies = new ProjectPolicies(projectMemberRepository, projectRoleRepository);
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
