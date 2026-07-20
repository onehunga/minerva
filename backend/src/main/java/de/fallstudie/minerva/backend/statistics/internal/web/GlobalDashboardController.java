package de.fallstudie.minerva.backend.statistics.internal.web;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import de.fallstudie.minerva.backend.statistics.internal.service.DashboardService;
import de.fallstudie.minerva.backend.user.Identity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/v1")
@RequiredArgsConstructor
public class GlobalDashboardController {
	private final DashboardService dashboardService;

	@GetMapping("/dashboard")
	public DashboardResponse getGlobalDashboard(@AuthenticationPrincipal Identity identity) {
		log.info("User with ID {} is requesting the global dashboard", identity.userId());
		return dashboardService.getGlobalDashboard(identity.userId());
	}
}
