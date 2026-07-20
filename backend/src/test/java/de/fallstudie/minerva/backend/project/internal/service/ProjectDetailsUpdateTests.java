package de.fallstudie.minerva.backend.project.internal.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import de.fallstudie.minerva.backend.common.ValidationException;
import de.fallstudie.minerva.backend.project.internal.persistence.ProjectMemberRepository;
import de.fallstudie.minerva.backend.project.internal.persistence.ProjectModel;
import de.fallstudie.minerva.backend.project.internal.persistence.ProjectRepository;
import de.fallstudie.minerva.backend.project.internal.persistence.ProjectRoleRepository;
import de.fallstudie.minerva.backend.project.internal.web.UpdateProjectDetailsRequest;
import de.fallstudie.minerva.backend.user.UserService;

class ProjectDetailsUpdateTests {
	private ProjectRepository projectRepository;
	private ProjectService projectService;

	@BeforeEach
	void setUp() {
		projectRepository = mock(ProjectRepository.class);
		projectService = new ProjectService(mock(ProjectMemberRepository.class), projectRepository,
				mock(ProjectRoleRepository.class), mock(UserService.class), mock());
	}

	@Test
	void updateProjectDetailsTrimsAndSavesDetails() {
		final var project = new ProjectModel();
		project.setName("Minerva");
		project.setDescription("Alt");
		when(projectRepository.findById(1L)).thenReturn(Optional.of(project));

		projectService.updateProjectDetails(1L,
				new UpdateProjectDetailsRequest(" Neues Minerva ", " Neue Beschreibung "));

		assertEquals("Neues Minerva", project.getName());
		assertEquals("Neue Beschreibung", project.getDescription());
		verify(projectRepository).save(project);
	}

	@Test
	void updateProjectDetailsRejectsInvalidDetails() {
		assertThrows(ValidationException.class, () -> projectService.updateProjectDetails(1L,
				new UpdateProjectDetailsRequest(" ", "Beschreibung")));
		assertThrows(ValidationException.class, () -> projectService.updateProjectDetails(1L,
				new UpdateProjectDetailsRequest("Minerva", "a".repeat(501))));

		verify(projectRepository, never()).save(org.mockito.ArgumentMatchers.any());
	}
}
