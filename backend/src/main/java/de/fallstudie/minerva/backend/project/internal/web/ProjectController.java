package de.fallstudie.minerva.backend.project.internal.web;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

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
	public ProjectRecordListResponse getAllProjects(@AuthenticationPrincipal Identity identity) {
		log.info("User with ID {} is requesting all projects", identity.userId());

		return projectService.getAllProjects(identity);
	}

	@GetMapping("/{id}")
	@PreAuthorize("@projectPolicy.canViewProject(principal, #id)")
	public ProjectDetailsResponse getProjectById(@AuthenticationPrincipal Identity identity,
			@PathVariable long id) {
		log.info("User with ID {} is requesting project with ID {}", identity.userId(), id);

		return projectService.getProjectById(identity, id);
	}

	@GetMapping("/{id}/users")
	@PreAuthorize("@projectPolicy.canManageProjectUsers(principal, #id)")
	public ProjectUserListResponse getProjectUsers(@AuthenticationPrincipal Identity identity,
			@PathVariable long id) {
		log.info("User with ID {} is requesting users for project with ID {}", identity.userId(),
				id);

		return projectService.getProjectUsers(identity, id);
	}

	@PostMapping("")
	public long createProject(@AuthenticationPrincipal Identity identity,
			@RequestBody CreateProjectRequest request) {
		log.info("Creating project with name '{}'", request.name());

		return projectService.createProject(identity, request);
	}

	@PostMapping("/{id}/users")
	@ResponseStatus(HttpStatus.CREATED)
	@PreAuthorize("@projectPolicy.canManageProjectUsers(principal, #id)")
	public void addProjectUser(@AuthenticationPrincipal Identity identity, @PathVariable long id,
			@RequestBody AddProjectUserRequest request) {
		log.info("User with ID {} is adding user {} to project with ID {}", identity.userId(),
				request.userId(), id);

		projectService.addProjectUser(identity, id, request);
	}
}
