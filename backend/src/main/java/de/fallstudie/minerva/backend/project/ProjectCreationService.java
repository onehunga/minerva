package de.fallstudie.minerva.backend.project;

import org.springframework.stereotype.Service;

import de.fallstudie.minerva.backend.project.internal.service.ProjectService;
import de.fallstudie.minerva.backend.user.Identity;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProjectCreationService {
	private final ProjectService projectService;

	@Transactional
	public long createProject(Identity identity, CreateProjectCommand command) {
		return projectService.createProject(identity, command);
	}
}
