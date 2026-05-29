package de.fallstudie.minerva.backend.project.internal.service;

import org.springframework.stereotype.Service;

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

	/**
	 * @return Die ID des neu erstellten Projekts
	 */
	public long createProject(Identity identity, CreateProjectRequest request) {
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
}
