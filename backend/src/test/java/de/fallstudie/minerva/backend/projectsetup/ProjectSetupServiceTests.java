package de.fallstudie.minerva.backend.projectsetup;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import de.fallstudie.minerva.backend.projectsetup.internal.service.ProjectSetupService;
import de.fallstudie.minerva.backend.projectsetup.internal.web.ProjectSetupRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import de.fallstudie.minerva.backend.common.DuplicateResourceException;
import de.fallstudie.minerva.backend.common.ValidationException;
import de.fallstudie.minerva.backend.project.CreateProjectCommand;
import de.fallstudie.minerva.backend.project.ProjectCreationService;
import de.fallstudie.minerva.backend.testsupport.TestProjects;
import de.fallstudie.minerva.backend.ticket.TicketConfigurationService;

class ProjectSetupServiceTests {
	private ProjectCreationService projectCreationService;
	private TicketConfigurationService ticketConfigurationService;
	private ProjectSetupService projectSetupService;

	@BeforeEach
	void setUp() {
		projectCreationService = Mockito.mock(ProjectCreationService.class);
		ticketConfigurationService = Mockito.mock(TicketConfigurationService.class);
		projectSetupService = new ProjectSetupService(projectCreationService,
				ticketConfigurationService);
	}

	@Test
	void createProjectCreatesProjectBeforeTicketConfiguration() {
		final var request = TestProjects.projectSetupRequest();
		final var command = new CreateProjectCommand("Minerva", "Ticket project");
		when(projectCreationService.createProject(TestProjects.OWNER, command))
				.thenReturn(TestProjects.PROJECT_ID);

		final var projectId = projectSetupService.createProject(TestProjects.OWNER, request);

		assertEquals(TestProjects.PROJECT_ID, projectId);
		final var inOrder = inOrder(projectCreationService, ticketConfigurationService);
		inOrder.verify(projectCreationService).createProject(TestProjects.OWNER, command);
		inOrder.verify(ticketConfigurationService)
				.createTicketConfiguration(TestProjects.PROJECT_ID, request.ticketConfiguration());
	}

	@Test
	void createProjectRejectsMissingTicketConfiguration() {
		final var request = new ProjectSetupRequest("Minerva", "Ticket project", null);

		assertThrows(ValidationException.class,
				() -> projectSetupService.createProject(TestProjects.OWNER, request));

		verifyNoInteractions(projectCreationService, ticketConfigurationService);
	}

	@Test
	void createProjectDoesNotConfigureTicketsWhenProjectCreationFails() {
		final var request = TestProjects.projectSetupRequest();
		final var command = new CreateProjectCommand("Minerva", "Ticket project");
		when(projectCreationService.createProject(TestProjects.OWNER, command))
				.thenThrow(new DuplicateResourceException("Projektname ist bereits vergeben"));

		assertThrows(DuplicateResourceException.class,
				() -> projectSetupService.createProject(TestProjects.OWNER, request));

		verifyNoInteractions(ticketConfigurationService);
	}

	@Test
	void createProjectPropagatesTicketConfigurationFailures() {
		final var request = TestProjects.projectSetupRequest();
		final var command = new CreateProjectCommand("Minerva", "Ticket project");
		when(projectCreationService.createProject(TestProjects.OWNER, command))
				.thenReturn(TestProjects.PROJECT_ID);
		doThrow(new ValidationException("Ticketkonfiguration ist ungültig"))
				.when(ticketConfigurationService)
				.createTicketConfiguration(TestProjects.PROJECT_ID, request.ticketConfiguration());

		assertThrows(ValidationException.class,
				() -> projectSetupService.createProject(TestProjects.OWNER, request));
	}
}
