package de.fallstudie.minerva.backend.notification.internal.service;

import de.fallstudie.minerva.backend.notification.internal.Notification;
import de.fallstudie.minerva.backend.notification.internal.NotificationType;
import de.fallstudie.minerva.backend.notification.internal.persistence.NotificationModel;
import de.fallstudie.minerva.backend.notification.internal.persistence.NotificationRepository;
import de.fallstudie.minerva.backend.testsupport.TestProjects;
import de.fallstudie.minerva.backend.ticket.TicketEvent;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;

class NotificationTests {
	@Test
	void assigneeChangeCreatesNotificationsWithTheCompleteChangeForBothUsers() {
		final var notificationService = Mockito.mock(NotificationService.class);
		final var handler = new NotificationEventHandler(notificationService);
		final var event = new TicketEvent.AssigneeChanged(TestProjects.OWNER_USER_ID,
				TestProjects.PROJECT_ID, TestProjects.CHILD_TICKET_ID,
				TestProjects.CONTRIBUTOR_USER_ID, TestProjects.VIEWER_USER_ID);

		handler.on(event);

		verify(notificationService).saveAll(List.of(
				new Notification.TicketUnassigned(TestProjects.CONTRIBUTOR_USER_ID,
						TestProjects.OWNER_USER_ID, TestProjects.PROJECT_ID,
						TestProjects.CHILD_TICKET_ID, TestProjects.CONTRIBUTOR_USER_ID,
						TestProjects.VIEWER_USER_ID),
				new Notification.TicketAssigned(TestProjects.VIEWER_USER_ID,
						TestProjects.OWNER_USER_ID, TestProjects.PROJECT_ID,
						TestProjects.CHILD_TICKET_ID, TestProjects.CONTRIBUTOR_USER_ID,
						TestProjects.VIEWER_USER_ID)));
	}

	@Test
	@SuppressWarnings("unchecked")
	void serviceStoresTheNotificationTypeAndAdtAsJson() throws JacksonException {
		final var repository = Mockito.mock(NotificationRepository.class);
		final var objectMapper = new ObjectMapper();
		final var service = new NotificationService(repository, objectMapper);
		final var notification = new Notification.TicketAssigned(TestProjects.VIEWER_USER_ID,
				TestProjects.OWNER_USER_ID, TestProjects.PROJECT_ID, TestProjects.CHILD_TICKET_ID,
				TestProjects.CONTRIBUTOR_USER_ID, TestProjects.VIEWER_USER_ID);

		service.saveAll(List.of(notification));

		final var modelsCaptor = ArgumentCaptor.forClass(List.class);
		verify(repository).saveAll(modelsCaptor.capture());
		final var model = ((List<NotificationModel>) modelsCaptor.getValue()).getFirst();
		assertEquals(TestProjects.VIEWER_USER_ID, model.getRecipientUserId());
		assertEquals(NotificationType.TICKET_ASSIGNED, model.getType());
		assertNotNull(model.getCreatedAt());
		final var payload = objectMapper.readTree(model.getPayloadJson());
		assertEquals(TestProjects.CONTRIBUTOR_USER_ID, payload.get("previousAssigneeId").asLong());
		assertEquals(TestProjects.VIEWER_USER_ID, payload.get("newAssigneeId").asLong());
	}
}
