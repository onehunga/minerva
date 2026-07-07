package de.fallstudie.minerva.backend.project.internal.service;

import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import de.fallstudie.minerva.backend.common.DuplicateResourceException;
import de.fallstudie.minerva.backend.common.ResourceNotFoundException;
import de.fallstudie.minerva.backend.common.ValidationException;
import de.fallstudie.minerva.backend.project.CreateProjectCommand;
import de.fallstudie.minerva.backend.project.internal.persistence.ProjectMemberModel;
import de.fallstudie.minerva.backend.project.internal.persistence.ProjectMemberRepository;
import de.fallstudie.minerva.backend.project.internal.persistence.ProjectModel;
import de.fallstudie.minerva.backend.project.internal.persistence.ProjectRepository;
import de.fallstudie.minerva.backend.project.internal.persistence.ProjectRoleModel;
import de.fallstudie.minerva.backend.project.internal.persistence.ProjectRoleName;
import de.fallstudie.minerva.backend.project.internal.persistence.ProjectRoleRepository;
import de.fallstudie.minerva.backend.project.internal.web.AddProjectUserRequest;
import de.fallstudie.minerva.backend.project.internal.web.ProjectDetailsResponse;
import de.fallstudie.minerva.backend.project.internal.web.ProjectRecordListResponse;
import de.fallstudie.minerva.backend.project.internal.web.ProjectRecordResponse;
import de.fallstudie.minerva.backend.project.internal.web.ProjectUserListResponse;
import de.fallstudie.minerva.backend.project.internal.web.ProjectUserResponse;
import de.fallstudie.minerva.backend.project.internal.web.UpdateProjectUserRoleRequest;
import de.fallstudie.minerva.backend.user.Identity;
import de.fallstudie.minerva.backend.user.UserService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProjectService {
	private final ProjectMemberRepository projectMemberRepository;
	private final ProjectRepository projectRepository;
	private final ProjectRoleRepository projectRoleRepository;
	private final UserService userService;

	public ProjectRecordListResponse getAllProjects(Identity identity) {
		final var projects = projectRepository.findAllByUserId(identity.userId()).stream()
				.map(project -> new ProjectRecordResponse(project.getId(), project.getName()))
				.toList();

		return new ProjectRecordListResponse(projects);
	}

	public ProjectDetailsResponse getProjectById(Identity identity, long projectId) {
		final var project = projectRepository.findById(projectId)
				.orElseThrow(() -> new ResourceNotFoundException(
						"Projekt mit ID " + projectId + " nicht gefunden"));
		final var member = projectMemberRepository
				.findByProjectIdAndUserId(projectId, identity.userId())
				.orElseThrow(() -> new ResourceNotFoundException(
						"Projekt mit ID " + projectId + " nicht gefunden"));
		final var projectRole = projectRoleRepository.findById(member.getRoleId())
				.orElseThrow(() -> new ResourceNotFoundException("Projektrolle nicht gefunden"));

		return new ProjectDetailsResponse(project.getId(), project.getName(),
				project.getDescription(), projectRole.getName());
	}

	public ProjectUserListResponse getProjectUsers(Identity identity, long projectId) {
		validateProjectExists(projectId);

		final var projectMembers = projectMemberRepository.findAllByProjectId(projectId).stream()
				.collect(Collectors.toMap(ProjectMemberModel::getUserId, Function.identity()));
		final var projectRoles = projectRoleRepository.findAllByProjectId(projectId).stream()
				.collect(Collectors.toMap(ProjectRoleModel::getId, ProjectRoleModel::getName));

		final var users = userService.findAll().stream().map(user -> {
			final var member = projectMembers.get(user.id());
			final var projectRole = member == null ? null : projectRoles.get(member.getRoleId());

			return new ProjectUserResponse(user.id(), user.username(), projectRole, member != null);
		}).toList();

		return new ProjectUserListResponse(users);
	}

	/**
	 * @return Die ID des neu erstellten Projekts
	 */
	@Transactional
	public long createProject(Identity identity, CreateProjectCommand command) {
		validateCreateProjectCommand(command);

		final var project = new ProjectModel();
		project.setName(command.name());
		project.setDescription(command.description());
		project.setCreatedBy(identity.userId());
		projectRepository.save(project);

		final var roles = createProjectRoles(project);
		final var ownerRole = roles[0];

		addProjectMember(project, ownerRole, identity);

		return project.getId();
	}

	@Transactional
	public void addProjectUser(Identity identity, long projectId, AddProjectUserRequest request) {
		validateProjectExists(projectId);
		validateAddProjectUserRequest(request);

		if (!userService.existsById(request.userId())) {
			throw new ResourceNotFoundException("Benutzer nicht gefunden");
		}

		if (projectMemberRepository.existsByProjectIdAndUserId(projectId, request.userId())) {
			throw new DuplicateResourceException("Benutzer ist bereits Teil dieses Projekts");
		}

		final var projectRoleName = validateProjectRole(request.role());
		final var role = projectRoleRepository.findByProjectIdAndName(projectId, projectRoleName)
				.orElseThrow(() -> new ValidationException("Projektrolle existiert nicht"));

		final var member = new ProjectMemberModel();
		member.setProjectId(projectId);
		member.setUserId(request.userId());
		member.setRoleId(role.getId());
		projectMemberRepository.save(member);
	}

	@Transactional
	public void updateProjectUserRole(Identity identity, long projectId, long userId,
			UpdateProjectUserRoleRequest request) {
		validateProjectExists(projectId);
		validateUpdateProjectUserRoleRequest(request);

		if (identity.userId() == userId) {
			throw new ValidationException("Ein Owner kann seine eigene Rolle nicht aktualisieren");
		}

		final var member = projectMemberRepository.findByProjectIdAndUserId(projectId, userId)
				.orElseThrow(() -> new ResourceNotFoundException("Projektmitglied nicht gefunden"));
		final var projectRoleName = validateProjectRole(request.role());
		final var role = projectRoleRepository.findByProjectIdAndName(projectId, projectRoleName)
				.orElseThrow(() -> new ValidationException("Projektrolle existiert nicht"));

		if (member.getRoleId() == role.getId()) {
			return;
		}

		member.setRoleId(role.getId());
		projectMemberRepository.save(member);
	}

	private ProjectRoleModel[] createProjectRoles(ProjectModel project) {
		ProjectRoleModel ownerRole = new ProjectRoleModel();
		ownerRole.setProjectId(project.getId());
		ownerRole.setName(ProjectRoleName.OWNER);
		projectRoleRepository.save(ownerRole);

		ProjectRoleModel contributorRole = new ProjectRoleModel();
		contributorRole.setProjectId(project.getId());
		contributorRole.setName(ProjectRoleName.CONTRIBUTOR);
		projectRoleRepository.save(contributorRole);

		ProjectRoleModel viewerRole = new ProjectRoleModel();
		viewerRole.setProjectId(project.getId());
		viewerRole.setName(ProjectRoleName.VIEWER);
		projectRoleRepository.save(viewerRole);

		return new ProjectRoleModel[]{ownerRole, contributorRole, viewerRole};
	}

	private void addProjectMember(ProjectModel project, ProjectRoleModel role, Identity identity) {
		final var member = new ProjectMemberModel();
		member.setProjectId(project.getId());
		member.setUserId(identity.userId());
		member.setRoleId(role.getId());
		projectMemberRepository.save(member);
	}

	private void validateCreateProjectCommand(CreateProjectCommand command) {
		if (command == null) {
			throw new IllegalArgumentException("Command must not be null");
		}

		if (command.name() == null || command.name().isBlank()) {
			throw new ValidationException("Projekt muss einen Namen haben");
		}

		if (command.description() != null && command.description().length() > 500) {
			throw new ValidationException("Projektbeschreibung darf maximal 500 Zeichen lang sein");
		}

		if (projectRepository.existsByName(command.name())) {
			throw new DuplicateResourceException("Projektname ist bereits vergeben");
		}
	}

	private void validateProjectExists(long projectId) {
		if (!projectRepository.existsById(projectId)) {
			throw new ResourceNotFoundException("Projekt mit ID " + projectId + " nicht gefunden");
		}
	}

	private void validateAddProjectUserRequest(AddProjectUserRequest request) {
		if (request == null) {
			throw new IllegalArgumentException("Request must not be null");
		}

		if (request.userId() <= 0) {
			throw new ValidationException("Benutzer ist erforderlich");
		}

		validateProjectRole(request.role());
	}

	private void validateUpdateProjectUserRoleRequest(UpdateProjectUserRoleRequest request) {
		if (request == null) {
			throw new IllegalArgumentException("Request must not be null");
		}

		validateProjectRole(request.role());
	}

	private ProjectRoleName validateProjectRole(String role) {
		if (role == null || role.isBlank()) {
			throw new ValidationException("Rolle ist erforderlich");
		}

		try {
			return ProjectRoleName.valueOf(role.trim());
		} catch (IllegalArgumentException exception) {
			throw new ValidationException("Rolle muss OWNER, CONTRIBUTOR oder VIEWER sein");
		}
	}
}
