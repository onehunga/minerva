package de.fallstudie.minerva.backend.project.internal.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.concurrent.atomic.AtomicLong;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

import de.fallstudie.minerva.backend.common.DuplicateResourceException;
import de.fallstudie.minerva.backend.common.ValidationException;
import de.fallstudie.minerva.backend.project.internal.persistence.ProjectMemberModel;
import de.fallstudie.minerva.backend.project.internal.persistence.ProjectMemberRepository;
import de.fallstudie.minerva.backend.project.internal.persistence.ProjectModel;
import de.fallstudie.minerva.backend.project.internal.persistence.ProjectRepository;
import de.fallstudie.minerva.backend.project.internal.persistence.ProjectRoleModel;
import de.fallstudie.minerva.backend.project.internal.persistence.ProjectRoleName;
import de.fallstudie.minerva.backend.project.internal.persistence.ProjectRoleRepository;
import de.fallstudie.minerva.backend.ticket.TicketConfigurationService;
import de.fallstudie.minerva.backend.testsupport.TestProjects;
import de.fallstudie.minerva.backend.user.UserService;

class ProjectCreationTests {
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
	void createProjectSavesProjectRolesOwnerMemberAndTicketConfiguration() {
		final var roleId = new AtomicLong(TestProjects.OWNER_ROLE_ID);
		final var request = TestProjects.createProjectRequest();
		when(projectRepository.existsByName("Minerva")).thenReturn(false);
		when(projectRepository.save(any(ProjectModel.class))).thenAnswer(invocation -> {
			final ProjectModel project = invocation.getArgument(0);
			ReflectionTestUtils.setField(project, "id", TestProjects.PROJECT_ID);
			return project;
		});
		when(projectRoleRepository.save(any(ProjectRoleModel.class))).thenAnswer(invocation -> {
			final ProjectRoleModel role = invocation.getArgument(0);
			ReflectionTestUtils.setField(role, "id", roleId.getAndIncrement());
			return role;
		});

		final long projectId = projectService.createProject(TestProjects.OWNER, request);

		assertEquals(TestProjects.PROJECT_ID, projectId);

		final var projectCaptor = ArgumentCaptor.forClass(ProjectModel.class);
		verify(projectRepository).save(projectCaptor.capture());
		final var savedProject = projectCaptor.getValue();
		assertEquals("Minerva", savedProject.getName());
		assertEquals("Ticket project", savedProject.getDescription());
		assertEquals(TestProjects.OWNER_USER_ID, savedProject.getCreatedBy());

		final var roleCaptor = ArgumentCaptor.forClass(ProjectRoleModel.class);
		verify(projectRoleRepository, Mockito.times(3)).save(roleCaptor.capture());
		final var savedRoles = roleCaptor.getAllValues();
		assertEquals(ProjectRoleName.OWNER, savedRoles.get(0).getName());
		assertEquals(ProjectRoleName.CONTRIBUTOR, savedRoles.get(1).getName());
		assertEquals(ProjectRoleName.VIEWER, savedRoles.get(2).getName());
		savedRoles.forEach(role -> assertEquals(TestProjects.PROJECT_ID, role.getProjectId()));

		final var memberCaptor = ArgumentCaptor.forClass(ProjectMemberModel.class);
		verify(projectMemberRepository).save(memberCaptor.capture());
		final var savedMember = memberCaptor.getValue();
		assertEquals(TestProjects.PROJECT_ID, savedMember.getProjectId());
		assertEquals(TestProjects.OWNER_USER_ID, savedMember.getUserId());
		assertEquals(TestProjects.OWNER_ROLE_ID, savedMember.getRoleId());
		verify(ticketConfigurationService).createTicketConfiguration(TestProjects.PROJECT_ID,
				request.workflowConfiguration());
	}

	@Test
	void createProjectRejectsMissingName() {
		assertInvalidProject(null, "description");
		assertInvalidProject("  ", "description");
	}

	@Test
	void createProjectRejectsLongDescription() {
		assertInvalidProject("Minerva", "a".repeat(501));
	}

	@Test
	void createProjectRejectsDuplicateName() {
		when(projectRepository.existsByName("Minerva")).thenReturn(true);

		assertThrows(DuplicateResourceException.class, () -> projectService
				.createProject(TestProjects.OWNER, TestProjects.createProjectRequest()));

		verify(projectRepository, never()).save(any());
		verify(projectRoleRepository, never()).save(any());
		verify(projectMemberRepository, never()).save(any());
		verify(ticketConfigurationService, never()).createTicketConfiguration(any(Long.class),
				any());
	}

	private void assertInvalidProject(String name, String description) {
		assertThrows(ValidationException.class,
				() -> projectService.createProject(TestProjects.OWNER,
						TestProjects.createProjectRequest(name, description)));

		verify(projectRepository, never()).save(any());
		verify(projectRoleRepository, never()).save(any());
		verify(projectMemberRepository, never()).save(any());
		verify(ticketConfigurationService, never()).createTicketConfiguration(any(Long.class),
				any());
	}
}
