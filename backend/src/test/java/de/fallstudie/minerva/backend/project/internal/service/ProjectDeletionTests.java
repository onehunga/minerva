package de.fallstudie.minerva.backend.project.internal.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.context.ApplicationEventPublisher;

import de.fallstudie.minerva.backend.common.ResourceNotFoundException;
import de.fallstudie.minerva.backend.project.ProjectDeletedEvent;
import de.fallstudie.minerva.backend.project.internal.persistence.ProjectMemberRepository;
import de.fallstudie.minerva.backend.project.internal.persistence.ProjectModel;
import de.fallstudie.minerva.backend.project.internal.persistence.ProjectRepository;
import de.fallstudie.minerva.backend.project.internal.persistence.ProjectRoleRepository;
import de.fallstudie.minerva.backend.testsupport.TestProjects;
import de.fallstudie.minerva.backend.user.UserService;

class ProjectDeletionTests {
	private ProjectRepository projectRepository;
	private ApplicationEventPublisher eventPublisher;
	private ProjectService projectService;

	@BeforeEach
	void setUp() {
		projectRepository = Mockito.mock(ProjectRepository.class);
		eventPublisher = Mockito.mock(ApplicationEventPublisher.class);
		projectService = new ProjectService(Mockito.mock(ProjectMemberRepository.class),
				projectRepository, Mockito.mock(ProjectRoleRepository.class),
				Mockito.mock(UserService.class), eventPublisher);
	}

	@Test
	void deleteProjectDeletesAndPublishesEvent() {
		final var project = TestProjects.project(TestProjects.PROJECT_ID);
		when(projectRepository.findById(TestProjects.PROJECT_ID)).thenReturn(Optional.of(project));

		projectService.deleteProject(TestProjects.PROJECT_ID);

		verify(projectRepository).delete(project);
		verify(projectRepository).flush();
		verify(eventPublisher).publishEvent(new ProjectDeletedEvent(TestProjects.PROJECT_ID));
	}

	@Test
	void deleteProjectDeletesArchivedProject() {
		final var project = TestProjects.project(TestProjects.PROJECT_ID);
		project.setArchivedAt(Instant.now());
		when(projectRepository.findById(TestProjects.PROJECT_ID)).thenReturn(Optional.of(project));

		projectService.deleteProject(TestProjects.PROJECT_ID);

		verify(projectRepository).delete(project);
		verify(projectRepository).flush();
		verify(eventPublisher).publishEvent(new ProjectDeletedEvent(TestProjects.PROJECT_ID));
	}

	@Test
	void deleteProjectRejectsUnknownProject() {
		when(projectRepository.findById(TestProjects.PROJECT_ID)).thenReturn(Optional.empty());

		assertThrows(ResourceNotFoundException.class,
				() -> projectService.deleteProject(TestProjects.PROJECT_ID));

		verify(projectRepository, never()).delete(Mockito.any(ProjectModel.class));
		verify(projectRepository, never()).flush();
		verify(eventPublisher, never()).publishEvent(Mockito.any());
	}
}
