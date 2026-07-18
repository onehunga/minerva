package de.fallstudie.minerva.backend.statistics.internal.web;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import de.fallstudie.minerva.backend.statistics.internal.service.DashboardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/v1/projects/{projectId}")
@RequiredArgsConstructor
public class ProjectDashboardController {
	private final DashboardService dashboardService;

	@GetMapping("/dashboard")
	@PreAuthorize("@projectPolicy.canViewProject(principal, #projectId)")
	public DashboardResponse getProjectDashboard(@PathVariable long projectId) {
		log.info("Requesting dashboard for project with ID {}", projectId);
		return dashboardService.getProjectDashboard(projectId);
	}
}
