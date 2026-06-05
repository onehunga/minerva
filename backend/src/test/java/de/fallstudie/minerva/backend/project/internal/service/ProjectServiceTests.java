package de.fallstudie.minerva.backend.project.internal.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

import de.fallstudie.minerva.backend.user.UserService;
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
import de.fallstudie.minerva.backend.project.internal.web.CreateProjectRequest;
import de.fallstudie.minerva.backend.ticket.TicketConfigurationService;
import de.fallstudie.minerva.backend.user.Identity;

class ProjectServiceTests {
	private static final Identity IDENTITY = new Identity(42L);

	private UserService userService;
	private ProjectMemberRepository projectMemberRepository;
	private ProjectRepository projectRepository;
	private ProjectRoleRepository projectRoleRepository;
	private TicketConfigurationService ticketConfigurationService;
	private ProjectService projectService;

	@BeforeEach
	void setUp() {
		userService = Mockito.mock(UserService.class);
		projectMemberRepository = org.mockito.Mockito.mock(ProjectMemberRepository.class);
		projectRepository = org.mockito.Mockito.mock(ProjectRepository.class);
		projectRoleRepository = org.mockito.Mockito.mock(ProjectRoleRepository.class);
		ticketConfigurationService = org.mockito.Mockito.mock(TicketConfigurationService.class);
		projectService = new ProjectService(projectMemberRepository, projectRepository,
				projectRoleRepository, ticketConfigurationService, userService);
	}

	@Test
	void getAllProjectsFiltersByIdentityUserId() {
		final var project = new ProjectModel();
		ReflectionTestUtils.setField(project, "id", 10L);
		project.setName("Minerva");
		project.setDescription("Ticket project");
		when(projectRepository.findAllByUserId(IDENTITY.userId())).thenReturn(List.of(project));

		final var response = projectService.getAllProjects(IDENTITY);

		assertEquals(1, response.projects().size());
		assertEquals(10L, response.projects().getFirst().id());
		assertEquals("Minerva", response.projects().getFirst().name());
		verify(projectRepository).findAllByUserId(IDENTITY.userId());
	}

	@Test
	void getProjectByIdReturnsProjectDetailsResponse() {
		final var project = new ProjectModel();
		ReflectionTestUtils.setField(project, "id", 10L);
		project.setName("Minerva");
		project.setDescription("Ticket project");
		final var member = new ProjectMemberModel();
		member.setRoleId(20L);
		final var projectRole = new ProjectRoleModel();
		projectRole.setName(ProjectRoleName.OWNER);
		when(projectRepository.findById(10L)).thenReturn(Optional.of(project));
		when(projectMemberRepository.findByProjectIdAndUserId(10L, IDENTITY.userId()))
				.thenReturn(Optional.of(member));
		when(projectRoleRepository.findById(20L)).thenReturn(Optional.of(projectRole));

		final var response = projectService.getProjectById(IDENTITY, 10L);

		assertEquals(10L, response.id());
		assertEquals("Minerva", response.name());
		assertEquals("Ticket project", response.description());
		assertEquals(ProjectRoleName.OWNER, response.projectRole());
		verify(projectRepository).findById(10L);
	}

	@Test
	void createProjectSavesProjectRolesAndOwnerMember() {
		final var roleId = new AtomicLong(100L);
		when(projectRepository.existsByName("Minerva")).thenReturn(false);
		when(projectRepository.save(any(ProjectModel.class))).thenAnswer(invocation -> {
			final ProjectModel project = invocation.getArgument(0);
			ReflectionTestUtils.setField(project, "id", 10L);
			return project;
		});
		when(projectRoleRepository.save(any(ProjectRoleModel.class))).thenAnswer(invocation -> {
			final ProjectRoleModel role = invocation.getArgument(0);
			ReflectionTestUtils.setField(role, "id", roleId.getAndIncrement());
			return role;
		});

		final long projectId = projectService.createProject(IDENTITY,
				createProjectRequest("Minerva", "Ticket project"));

		assertEquals(10L, projectId);

		ArgumentCaptor<ProjectModel> projectCaptor = ArgumentCaptor.forClass(ProjectModel.class);
		verify(projectRepository).save(projectCaptor.capture());
		final ProjectModel savedProject = projectCaptor.getValue();
		assertEquals("Minerva", savedProject.getName());
		assertEquals("Ticket project", savedProject.getDescription());
		assertEquals(IDENTITY.userId(), savedProject.getCreatedBy());

		ArgumentCaptor<ProjectRoleModel> roleCaptor = ArgumentCaptor
				.forClass(ProjectRoleModel.class);
		verify(projectRoleRepository, org.mockito.Mockito.times(3)).save(roleCaptor.capture());
		final var savedRoles = roleCaptor.getAllValues();
		assertEquals(ProjectRoleName.OWNER, savedRoles.get(0).getName());
		assertEquals(ProjectRoleName.CONTRIBUTOR, savedRoles.get(1).getName());
		assertEquals(ProjectRoleName.VIEWER, savedRoles.get(2).getName());
		savedRoles.forEach(role -> assertEquals(projectId, role.getProjectId()));

		ArgumentCaptor<ProjectMemberModel> memberCaptor = ArgumentCaptor
				.forClass(ProjectMemberModel.class);
		verify(projectMemberRepository).save(memberCaptor.capture());
		final ProjectMemberModel savedMember = memberCaptor.getValue();
		assertEquals(projectId, savedMember.getProjectId());
		assertEquals(IDENTITY.userId(), savedMember.getUserId());
		assertEquals(100L, savedMember.getRoleId());
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

		assertThrows(DuplicateResourceException.class, () -> projectService.createProject(IDENTITY,
				createProjectRequest("Minerva", "description")));

		verify(projectRepository, never()).save(any());
		verify(projectRoleRepository, never()).save(any());
		verify(projectMemberRepository, never()).save(any());
	}

	private void assertInvalidProject(String name, String description) {
		assertThrows(ValidationException.class, () -> projectService.createProject(IDENTITY,
				createProjectRequest(name, description)));

		verify(projectRepository, never()).save(any());
		verify(projectRoleRepository, never()).save(any());
		verify(projectMemberRepository, never()).save(any());
	}

	private CreateProjectRequest createProjectRequest(String name, String description) {
		return new CreateProjectRequest(name, description, null);
	}
}
