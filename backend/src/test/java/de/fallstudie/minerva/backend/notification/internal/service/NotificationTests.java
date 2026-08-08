package de.fallstudie.minerva.backend.notification.internal.service;

import de.fallstudie.minerva.backend.notification.internal.Notification;
import de.fallstudie.minerva.backend.notification.internal.NotificationType;
import de.fallstudie.minerva.backend.notification.internal.persistence.NotificationModel;
import de.fallstudie.minerva.backend.notification.internal.persistence.NotificationRepository;
import de.fallstudie.minerva.backend.realtime.UserEventStream;
import de.fallstudie.minerva.backend.testsupport.TestProjects;
import de.fallstudie.minerva.backend.ticket.TicketEvent;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class NotificationTests {
	@Test
	void assigneeChangeCreatesNotificationsWithTheCompleteChangeForBothUsers() {
		final var notificationService = Mockito.mock(NotificationService.class);
		final var userEventStream = Mockito.mock(UserEventStream.class);
		final var handler = new NotificationEventHandler(notificationService, userEventStream);
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
	void createdNotificationsInvalidateTheNotificationDataOfTheirRecipient() {
		final var notificationService = Mockito.mock(NotificationService.class);
		final var userEventStream = Mockito.mock(UserEventStream.class);
		final var handler = new NotificationEventHandler(notificationService, userEventStream);
		when(notificationService.saveAll(Mockito.any()))
				.thenReturn(List.of(TestProjects.VIEWER_USER_ID));

		handler.on(new TicketEvent.TicketCreated(TestProjects.OWNER_USER_ID,
				TestProjects.PROJECT_ID, TestProjects.CHILD_TICKET_ID, 1L, 1L, "Ticket",
				TestProjects.VIEWER_USER_ID));

		verify(userEventStream).invalidate(TestProjects.VIEWER_USER_ID, "notifications");
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

		when(repository.saveAll(Mockito.any())).thenAnswer(invocation -> invocation.getArgument(0));
		final var recipientUserIds = service.saveAll(List.of(notification));

		assertEquals(List.of(TestProjects.VIEWER_USER_ID), recipientUserIds);
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

	@Test
	void serviceMarksOnlyTheRecipientsNotificationAsRead() {
		final var repository = Mockito.mock(NotificationRepository.class);
		final var service = new NotificationService(repository, new ObjectMapper());
		final var notification = new NotificationModel();
		notification.setId(12L);
		notification.setRecipientUserId(TestProjects.VIEWER_USER_ID);
		when(repository.findByIdAndRecipientUserId(12L, TestProjects.VIEWER_USER_ID))
				.thenReturn(Optional.of(notification));

		service.markAsRead(TestProjects.VIEWER_USER_ID, 12L);

		assertNotNull(notification.getReadAt());
		verify(repository).findByIdAndRecipientUserId(12L, TestProjects.VIEWER_USER_ID);
		verify(repository).save(notification);
	}
}
