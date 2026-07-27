package de.fallstudie.minerva.backend.project.internal.web;

import de.fallstudie.minerva.backend.project.internal.service.ProjectService;
import de.fallstudie.minerva.backend.user.Identity;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/admin/projects")
@RequiredArgsConstructor
public class AdminProjectController {
	private final ProjectService projectService;

	@GetMapping
	@PreAuthorize("@userPolicies.isAdmin(principal)")
	public ProjectRecordListResponse getProjects(@AuthenticationPrincipal Identity identity) {
		return projectService.getAdminProjects(identity);
	}
}
