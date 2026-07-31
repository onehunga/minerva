package de.fallstudie.minerva.backend.projectsetup.internal.web;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import de.fallstudie.minerva.backend.projectsetup.internal.service.ProjectSetupService;
import de.fallstudie.minerva.backend.user.Identity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/v1/projects")
@RequiredArgsConstructor
public class ProjectSetupController {
	private final ProjectSetupService projectSetupService;

	@PostMapping("")
	public long createProject(@AuthenticationPrincipal Identity identity,
			@RequestBody ProjectSetupRequest request) {
		log.info("Creating project with name '{}'", request == null ? null : request.name());

		return projectSetupService.createProject(identity, request);
	}
}
