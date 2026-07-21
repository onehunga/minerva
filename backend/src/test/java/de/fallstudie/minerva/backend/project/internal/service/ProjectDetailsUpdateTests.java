package de.fallstudie.minerva.backend.project.internal.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.time.Instant;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;

import de.fallstudie.minerva.backend.common.ValidationException;
import de.fallstudie.minerva.backend.common.ReadOnlyException;
import de.fallstudie.minerva.backend.project.ProjectEvent;
import de.fallstudie.minerva.backend.project.internal.persistence.ProjectMemberRepository;
import de.fallstudie.minerva.backend.project.internal.persistence.ProjectModel;
import de.fallstudie.minerva.backend.project.internal.persistence.ProjectRepository;
import de.fallstudie.minerva.backend.project.internal.persistence.ProjectRoleRepository;
import de.fallstudie.minerva.backend.project.internal.web.UpdateProjectDetailsRequest;
import de.fallstudie.minerva.backend.testsupport.TestProjects;
import de.fallstudie.minerva.backend.user.UserService;

class ProjectDetailsUpdateTests {
	private ProjectRepository projectRepository;
	private ApplicationEventPublisher eventPublisher;
	private ProjectService projectService;

	@BeforeEach
	void setUp() {
		projectRepository = mock(ProjectRepository.class);
		eventPublisher = mock(ApplicationEventPublisher.class);
		projectService = new ProjectService(mock(ProjectMemberRepository.class), projectRepository,
				mock(ProjectRoleRepository.class), mock(UserService.class), eventPublisher);
	}

	@Test
	void updateProjectDetailsTrimsAndSavesDetails() {
		final var project = new ProjectModel();
		project.setName("Minerva");
		project.setDescription("Alt");
		when(projectRepository.findById(1L)).thenReturn(Optional.of(project));

		projectService.updateProjectDetails(TestProjects.OWNER, 1L,
				new UpdateProjectDetailsRequest(" Neues Minerva ", " Neue Beschreibung "));

		assertEquals("Neues Minerva", project.getName());
		assertEquals("Neue Beschreibung", project.getDescription());
		verify(projectRepository).save(project);
		verify(eventPublisher)
				.publishEvent(new ProjectEvent.DetailsUpdated(TestProjects.OWNER_USER_ID, 1L,
						"Minerva", "Neues Minerva", "Alt", "Neue Beschreibung"));
	}

	@Test
	void updateProjectDetailsDoesNotPublishEventForUnchangedDetails() {
		final var project = new ProjectModel();
		project.setName("Minerva");
		project.setDescription("Beschreibung");
		when(projectRepository.findById(1L)).thenReturn(Optional.of(project));

		projectService.updateProjectDetails(TestProjects.OWNER, 1L,
				new UpdateProjectDetailsRequest(" Minerva ", " Beschreibung "));

		verify(eventPublisher, never()).publishEvent(org.mockito.ArgumentMatchers.any());
	}

	@Test
	void updateProjectDetailsRejectsInvalidDetails() {
		assertThrows(ValidationException.class,
				() -> projectService.updateProjectDetails(TestProjects.OWNER, 1L,
						new UpdateProjectDetailsRequest(" ", "Beschreibung")));
		assertThrows(ValidationException.class,
				() -> projectService.updateProjectDetails(TestProjects.OWNER, 1L,
						new UpdateProjectDetailsRequest("Minerva", "a".repeat(501))));

		verify(projectRepository, never()).save(org.mockito.ArgumentMatchers.any());
	}

	@Test
	void updateProjectDetailsRejectsArchivedProject() {
		final var project = new ProjectModel();
		project.setName("Minerva");
		project.setArchivedAt(Instant.now());
		when(projectRepository.findById(1L)).thenReturn(Optional.of(project));

		assertThrows(ReadOnlyException.class,
				() -> projectService.updateProjectDetails(TestProjects.OWNER, 1L,
						new UpdateProjectDetailsRequest("Neu", "Beschreibung")));

		verify(projectRepository, never()).save(org.mockito.ArgumentMatchers.any());
		verify(eventPublisher, never()).publishEvent(org.mockito.ArgumentMatchers.any());
	}
}
