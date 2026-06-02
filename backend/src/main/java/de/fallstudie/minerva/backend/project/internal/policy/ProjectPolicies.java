package de.fallstudie.minerva.backend.project.internal.policy;

import org.springframework.stereotype.Component;

import de.fallstudie.minerva.backend.project.internal.persistence.ProjectMemberRepository;
import de.fallstudie.minerva.backend.user.Identity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component("projectPolicy")
@RequiredArgsConstructor
public class ProjectPolicies {
	private final ProjectMemberRepository projectMemberRepository;

	public boolean canViewProject(Identity userId, long projectId) {
		log.trace("Checking if user {} can view project {}", userId, projectId);

		return projectMemberRepository.existsByProjectIdAndUserId(projectId, userId.userId());
	}
}
