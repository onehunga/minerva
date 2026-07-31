package de.fallstudie.minerva.backend.projectsetup.internal.service;

import de.fallstudie.minerva.backend.common.ValidationException;
import de.fallstudie.minerva.backend.project.CreateProjectCommand;
import de.fallstudie.minerva.backend.project.ProjectCreationService;
import de.fallstudie.minerva.backend.projectsetup.internal.web.ProjectSetupRequest;
import de.fallstudie.minerva.backend.ticket.TicketConfigurationService;
import de.fallstudie.minerva.backend.user.Identity;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProjectSetupService {
	private final ProjectCreationService projectCreationService;
	private final TicketConfigurationService ticketConfigurationService;

	@Transactional
	public long createProject(Identity identity, ProjectSetupRequest request) {
		log.trace("called createProject");
		validateProjectSetupRequest(request);

		final var projectId = projectCreationService.createProject(identity,
				new CreateProjectCommand(request.name(), request.description()));

		log.debug("creating workflow for project ({})", projectId);
		ticketConfigurationService.createTicketConfiguration(projectId,
				request.ticketConfiguration());

		return projectId;
	}

	private void validateProjectSetupRequest(ProjectSetupRequest request) {
		if (request == null) {
			throw new ValidationException("Projekt-Setup ist erforderlich");
		}

		if (request.ticketConfiguration() == null) {
			throw new ValidationException("Ticketkonfiguration ist erforderlich");
		}
	}
}
