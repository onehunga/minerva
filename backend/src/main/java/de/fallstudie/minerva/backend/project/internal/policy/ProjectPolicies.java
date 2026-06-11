package de.fallstudie.minerva.backend.project.internal.policy;

import de.fallstudie.minerva.backend.project.internal.persistence.ProjectRoleRepository;
import org.springframework.stereotype.Component;

import de.fallstudie.minerva.backend.project.internal.persistence.ProjectMemberRepository;
import de.fallstudie.minerva.backend.project.internal.persistence.ProjectRoleName;
import de.fallstudie.minerva.backend.user.Identity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component("projectPolicy")
@RequiredArgsConstructor
public class ProjectPolicies {
	private final ProjectMemberRepository projectMemberRepository;
	private final ProjectRoleRepository projectRoleRepository;

	public boolean canViewProject(Identity userId, long projectId) {
		log.trace("Checking if user {} can view project {}", userId, projectId);

		return projectMemberRepository.existsByProjectIdAndUserId(projectId, userId.userId());
	}

	public boolean canManageProjectUsers(Identity identity, long projectId) {
		log.trace("Checking if user {} can manage project users for project {}", identity,
				projectId);

		if (!projectMemberRepository.existsByProjectIdAndUserId(projectId, identity.userId())) {
			return false;
		}

		final var roleId = projectMemberRepository
				.findByProjectIdAndUserId(projectId, identity.userId())
				.orElseThrow(() -> new IllegalStateException(
						"User is not a member of the project, but existence was checked before"))
				.getRoleId();
		final var projectRole = projectRoleRepository.findById(roleId)
				.orElseThrow(() -> new IllegalStateException("Role with id " + roleId
						+ " does not exist, but existence was checked before"));
		return projectRole.getName() == ProjectRoleName.OWNER;
	}

	public boolean canModifyTickets(Identity identity, long projectId) {
		log.trace("Checking if user {} can modify tickets for project {}", identity, projectId);

		if (!projectMemberRepository.existsByProjectIdAndUserId(projectId, identity.userId())) {
			return false;
		}

		final var roleId = projectMemberRepository
				.findByProjectIdAndUserId(projectId, identity.userId())
				.orElseThrow(() -> new IllegalStateException(
						"User is not a member of the project, but existence was checked before"))
				.getRoleId();
		final var projectRole = projectRoleRepository.findById(roleId)
				.orElseThrow(() -> new IllegalStateException("Role with id " + roleId
						+ " does not exist, but existence was checked before"));

		return projectRole.getName() == ProjectRoleName.OWNER
				|| projectRole.getName() == ProjectRoleName.CONTRIBUTOR;
	}
}
