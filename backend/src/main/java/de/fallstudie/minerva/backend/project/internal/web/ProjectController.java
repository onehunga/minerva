package de.fallstudie.minerva.backend.project.internal.web;

import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import de.fallstudie.minerva.backend.project.internal.persistence.ProjectModel;
import de.fallstudie.minerva.backend.project.internal.service.ProjectService;
import de.fallstudie.minerva.backend.user.Identity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/v1/projects")
@RequiredArgsConstructor
public class ProjectController {
	private final ProjectService projectService;

	@GetMapping("")
	public List<ProjectModel> getAllProjects(@AuthenticationPrincipal Identity identity) {
		log.info("User with ID {} is requesting all projects", identity.userId());

		return projectService.getAllProjects(identity);
	}

	@PostMapping("")
	public long createProject(@AuthenticationPrincipal Identity identity,
			@RequestBody CreateProjectRequest request) {
		log.info("Creating project with name '{}'", request.name());

		return projectService.createProject(identity, request);
	}
}
