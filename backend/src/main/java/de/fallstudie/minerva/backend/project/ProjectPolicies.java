package de.fallstudie.minerva.backend.project;

import de.fallstudie.minerva.backend.project.internal.persistence.ProjectRoleRepository;
import de.fallstudie.minerva.backend.project.internal.persistence.ProjectRepository;
import org.springframework.stereotype.Component;

import de.fallstudie.minerva.backend.project.internal.persistence.ProjectMemberRepository;
import de.fallstudie.minerva.backend.project.internal.persistence.ProjectRoleName;
import de.fallstudie.minerva.backend.user.Identity;
import de.fallstudie.minerva.backend.user.UserService;
import de.fallstudie.minerva.backend.user.WorkspaceRoleName;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component("projectPolicy")
@RequiredArgsConstructor
public class ProjectPolicies {
	private final ProjectMemberRepository projectMemberRepository;
	private final ProjectRoleRepository projectRoleRepository;
	private final ProjectRepository projectRepository;
	private final UserService userService;

	public boolean isArchived(long projectId) {
		return projectRepository.existsByIdAndArchivedAtIsNotNull(projectId);
	}

	public boolean canViewProject(Identity userId, long projectId) {
		log.trace("Checking if user {} can view project {}", userId, projectId);

		return isAdmin(userId)
				|| projectMemberRepository.existsByProjectIdAndUserId(projectId, userId.userId());
	}

	public boolean canManageProjectUsers(Identity identity, long projectId) {
		log.trace("Checking if user {} can manage project users for project {}", identity,
				projectId);

		if (isAdmin(identity)) {
			return true;
		}

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

	public boolean canUpdateProjectUserRole(Identity identity, long projectId) {
		return canManageProjectUsers(identity, projectId);
	}

	public boolean canModifyTickets(Identity identity, long projectId) {
		log.trace("Checking if user {} can modify tickets for project {}", identity, projectId);

		if (isAdmin(identity)) {
			return true;
		}

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

	public boolean canBeAssigned(long projectId, long userId) {
		log.trace("Checking if user {} can be assigned in project {}", userId, projectId);

		if (!projectMemberRepository.existsByProjectIdAndUserId(projectId, userId)) {
			return false;
		}

		final var roleId = projectMemberRepository.findByProjectIdAndUserId(projectId, userId)
				.orElseThrow(() -> new IllegalStateException(
						"User is not a member of the project, but existence was checked before"))
				.getRoleId();
		final var projectRole = projectRoleRepository.findById(roleId)
				.orElseThrow(() -> new IllegalStateException("Role with id " + roleId
						+ " does not exist, but existence was checked before"));

		return projectRole.getName() == ProjectRoleName.OWNER
				|| projectRole.getName() == ProjectRoleName.CONTRIBUTOR;
	}

	private boolean isAdmin(Identity identity) {
		return userService.findActiveById(identity.userId())
				.map(user -> user.workspaceRole() == WorkspaceRoleName.ADMIN).orElse(false);
	}
}
