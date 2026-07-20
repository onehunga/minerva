package de.fallstudie.minerva.backend.project.internal.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.context.ApplicationEventPublisher;

import de.fallstudie.minerva.backend.common.ResourceNotFoundException;
import de.fallstudie.minerva.backend.project.internal.persistence.ProjectMemberRepository;
import de.fallstudie.minerva.backend.project.internal.persistence.ProjectRepository;
import de.fallstudie.minerva.backend.project.internal.persistence.ProjectRoleName;
import de.fallstudie.minerva.backend.project.internal.persistence.ProjectRoleRepository;
import de.fallstudie.minerva.backend.testsupport.TestProjects;
import de.fallstudie.minerva.backend.user.UserService;

class ProjectQueryTests {
	private UserService userService;
	private ProjectMemberRepository projectMemberRepository;
	private ProjectRepository projectRepository;
	private ProjectRoleRepository projectRoleRepository;
	private ProjectService projectService;

	@BeforeEach
	void setUp() {
		userService = Mockito.mock(UserService.class);
		projectMemberRepository = Mockito.mock(ProjectMemberRepository.class);
		projectRepository = Mockito.mock(ProjectRepository.class);
		projectRoleRepository = Mockito.mock(ProjectRoleRepository.class);
		projectService = new ProjectService(projectMemberRepository, projectRepository,
				projectRoleRepository, userService, Mockito.mock(ApplicationEventPublisher.class));
	}

	@Test
	void getAllProjectsFiltersByIdentityUserId() {
		final var project = TestProjects.project(TestProjects.PROJECT_ID);
		when(projectRepository.findAllByUserId(TestProjects.OWNER_USER_ID))
				.thenReturn(List.of(project));

		final var response = projectService.getAllProjects(TestProjects.OWNER);

		assertEquals(1, response.projects().size());
		assertEquals(TestProjects.PROJECT_ID, response.projects().getFirst().id());
		assertEquals("Minerva", response.projects().getFirst().name());
	}

	@Test
	void getProjectByIdReturnsProjectDetailsResponseWithRole() {
		final var project = TestProjects.project(TestProjects.PROJECT_ID);
		final var member = TestProjects.projectMember(TestProjects.PROJECT_ID,
				TestProjects.OWNER_USER_ID, TestProjects.OWNER_ROLE_ID);
		final var role = TestProjects.projectRole(TestProjects.OWNER_ROLE_ID,
				TestProjects.PROJECT_ID, ProjectRoleName.OWNER);
		when(projectRepository.findById(TestProjects.PROJECT_ID)).thenReturn(Optional.of(project));
		when(projectMemberRepository.findByProjectIdAndUserId(TestProjects.PROJECT_ID,
				TestProjects.OWNER_USER_ID)).thenReturn(Optional.of(member));
		when(projectRoleRepository.findById(TestProjects.OWNER_ROLE_ID))
				.thenReturn(Optional.of(role));

		final var response = projectService.getProjectById(TestProjects.OWNER,
				TestProjects.PROJECT_ID);

		assertEquals(TestProjects.PROJECT_ID, response.id());
		assertEquals("Minerva", response.name());
		assertEquals("Ticket project", response.description());
		assertEquals(ProjectRoleName.OWNER, response.projectRole());
		assertFalse(response.archived());
	}

	@Test
	void getProjectByIdRejectsUnknownProject() {
		when(projectRepository.findById(TestProjects.PROJECT_ID)).thenReturn(Optional.empty());

		assertThrows(ResourceNotFoundException.class,
				() -> projectService.getProjectById(TestProjects.OWNER, TestProjects.PROJECT_ID));
	}

	@Test
	void getProjectByIdRejectsUserWithoutMembership() {
		when(projectRepository.findById(TestProjects.PROJECT_ID))
				.thenReturn(Optional.of(TestProjects.project(TestProjects.PROJECT_ID)));
		when(projectMemberRepository.findByProjectIdAndUserId(TestProjects.PROJECT_ID,
				TestProjects.OWNER_USER_ID)).thenReturn(Optional.empty());

		assertThrows(ResourceNotFoundException.class,
				() -> projectService.getProjectById(TestProjects.OWNER, TestProjects.PROJECT_ID));
	}

	@Test
	void getProjectByIdRejectsMissingProjectRole() {
		final var member = TestProjects.projectMember(TestProjects.PROJECT_ID,
				TestProjects.OWNER_USER_ID, TestProjects.OWNER_ROLE_ID);
		when(projectRepository.findById(TestProjects.PROJECT_ID))
				.thenReturn(Optional.of(TestProjects.project(TestProjects.PROJECT_ID)));
		when(projectMemberRepository.findByProjectIdAndUserId(TestProjects.PROJECT_ID,
				TestProjects.OWNER_USER_ID)).thenReturn(Optional.of(member));
		when(projectRoleRepository.findById(TestProjects.OWNER_ROLE_ID))
				.thenReturn(Optional.empty());

		assertThrows(ResourceNotFoundException.class,
				() -> projectService.getProjectById(TestProjects.OWNER, TestProjects.PROJECT_ID));
	}

	@Test
	void archiveProjectArchivesExistingProject() {
		final var project = TestProjects.project(TestProjects.PROJECT_ID);
		when(projectRepository.findById(TestProjects.PROJECT_ID)).thenReturn(Optional.of(project));

		projectService.archiveProject(TestProjects.PROJECT_ID);

		assertNotNull(project.getArchivedAt());
		verify(projectRepository).save(project);
		verify(projectRepository).flush();
	}

	@Test
	void archiveProjectRejectsUnknownProject() {
		when(projectRepository.findById(TestProjects.PROJECT_ID)).thenReturn(Optional.empty());

		assertThrows(ResourceNotFoundException.class,
				() -> projectService.archiveProject(TestProjects.PROJECT_ID));
		verify(projectRepository, never()).save(Mockito.any());
		verify(projectRepository, never()).flush();
	}
}
