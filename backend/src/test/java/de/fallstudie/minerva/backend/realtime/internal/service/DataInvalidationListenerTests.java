package de.fallstudie.minerva.backend.realtime.internal.service;

import de.fallstudie.minerva.backend.project.ProjectEvent;
import de.fallstudie.minerva.backend.realtime.UserEventStream;
import de.fallstudie.minerva.backend.testsupport.TestProjects;
import de.fallstudie.minerva.backend.ticket.TicketEvent;
import de.fallstudie.minerva.backend.user.UserEvent;
import de.fallstudie.minerva.backend.user.WorkspaceRoleName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.mockito.Mockito.verify;

class DataInvalidationListenerTests {
	private final UserEventStream userEventStream = Mockito.mock(UserEventStream.class);
	private final DataInvalidationListener listener = new DataInvalidationListener(userEventStream);

	@Test
	void ticketEventsInvalidateTheTicketListOfTheProject() {
		listener.on(new TicketEvent.StatusChanged(TestProjects.OWNER_USER_ID,
				TestProjects.PROJECT_ID, TestProjects.CHILD_TICKET_ID, 1L, "Open", 2L, "Done", 3L,
				"Close", TestProjects.VIEWER_USER_ID));

		verify(userEventStream)
				.invalidate("projects:%d:tickets".formatted(TestProjects.PROJECT_ID));
	}

	@Test
	void projectMembershipEventsInvalidateTheProjectUsers() {
		listener.on(new ProjectEvent.UserAdded(TestProjects.OWNER_USER_ID, TestProjects.PROJECT_ID,
				TestProjects.VIEWER_USER_ID, "VIEWER"));

		verify(userEventStream).invalidate("projects:%d:users".formatted(TestProjects.PROJECT_ID));
	}

	@Test
	void projectLifecycleEventsInvalidateTheProjectList() {
		listener.on(new ProjectEvent.ProjectCreated(TestProjects.OWNER_USER_ID,
				TestProjects.PROJECT_ID, "Project", "Description"));

		verify(userEventStream).invalidate("projects");
	}

	@Test
	void userEventsInvalidateTheUserList() {
		listener.on(new UserEvent.Created(TestProjects.OWNER_USER_ID, TestProjects.VIEWER_USER_ID,
				"user", WorkspaceRoleName.USER));

		verify(userEventStream).invalidate("users");
	}
}
