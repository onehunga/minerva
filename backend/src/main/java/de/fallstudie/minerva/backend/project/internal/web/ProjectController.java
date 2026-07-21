package de.fallstudie.minerva.backend.project.internal.web;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
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

	@PatchMapping("/{id}/details")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@PreAuthorize("@projectPolicy.canManageProjectUsers(principal, #id)")
	public void updateProjectDetails(@AuthenticationPrincipal Identity identity,
			@PathVariable long id, @RequestBody UpdateProjectDetailsRequest request) {
		log.info("User with ID {} is updating details for project with ID {}", identity.userId(),
				id);

		projectService.updateProjectDetails(identity, id, request);
	}

	@PatchMapping("/{id}/archive")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@PreAuthorize("@projectPolicy.canManageProjectUsers(principal, #id)")
	public void archiveProject(@AuthenticationPrincipal Identity identity, @PathVariable long id) {
		log.info("User with ID {} is archiving project with ID {}", identity.userId(), id);

		projectService.archiveProject(identity, id);
	}

	@GetMapping("/{id}/users")
	@PreAuthorize("@projectPolicy.canViewProject(principal, #id)")
	public ProjectUserListResponse getProjectUsers(@AuthenticationPrincipal Identity identity,
			@PathVariable long id) {
		log.info("User with ID {} is requesting users for project with ID {}", identity.userId(),
				id);

		return projectService.getProjectUsers(identity, id);
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

	@PatchMapping("/{id}/users/{userId}/role")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@PreAuthorize("@projectPolicy.canUpdateProjectUserRole(principal, #id)")
	public void updateProjectUserRole(@AuthenticationPrincipal Identity identity,
			@PathVariable long id, @PathVariable long userId,
			@RequestBody UpdateProjectUserRoleRequest request) {
		log.info("User with ID {} is updating project role for user {} in project with ID {}",
				identity.userId(), userId, id);

		projectService.updateProjectUserRole(identity, id, userId, request);
	}

	@DeleteMapping("/{id}/users/{userId}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@PreAuthorize("@projectPolicy.canManageProjectUsers(principal, #id)")
	public void removeProjectUser(@AuthenticationPrincipal Identity identity, @PathVariable long id,
			@PathVariable long userId) {
		log.info("User with ID {} is removing user {} from project with ID {}", identity.userId(),
				userId, id);

		projectService.removeProjectUser(identity, id, userId);
	}
}
