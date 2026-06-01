package de.fallstudie.minerva.backend.project.internal.service;

import java.util.List;

import org.springframework.stereotype.Service;

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
import de.fallstudie.minerva.backend.user.Identity;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProjectService {
	private final ProjectMemberRepository projectMemberRepository;
	private final ProjectRepository projectRepository;
	private final ProjectRoleRepository projectRoleRepository;

	public List<ProjectModel> getAllProjects(Identity identity) {
		return projectRepository.findAllByUserId(identity.userId());
	}

	/**
	 * @return Die ID des neu erstellten Projekts
	 */
	public long createProject(Identity identity, CreateProjectRequest request) {
		validateCreateProjectRequest(request);

		final var project = new ProjectModel();
		project.setName(request.name());
		project.setDescription(request.description());
		project.setCreatedBy(identity.userId());
		projectRepository.save(project);

		final var roles = createProjectRoles(project);
		final var ownerRole = roles[0];

		addProjectMember(project, ownerRole, identity);

		return project.getId();
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

	private void validateCreateProjectRequest(CreateProjectRequest request) {
		if (request == null) {
			throw new IllegalArgumentException("Request must not be null");
		}

		if (request.name() == null || request.name().isBlank()) {
			throw new ValidationException("Projekt muss einen Namen haben");
		}

		if (request.description() != null && request.description().length() > 500) {
			throw new ValidationException("Projektbeschreibung darf maximal 500 Zeichen lang sein");
		}

		if (projectRepository.existsByName(request.name())) {
			throw new DuplicateResourceException("Projektname ist bereits vergeben");
		}
	}
}
