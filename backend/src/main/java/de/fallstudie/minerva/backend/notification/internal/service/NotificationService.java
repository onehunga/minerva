package de.fallstudie.minerva.backend.notification.internal.service;

import de.fallstudie.minerva.backend.notification.internal.Notification;
import de.fallstudie.minerva.backend.notification.internal.NotificationType;
import de.fallstudie.minerva.backend.notification.internal.persistence.NotificationModel;
import de.fallstudie.minerva.backend.notification.internal.persistence.NotificationRepository;
import de.fallstudie.minerva.backend.notification.internal.web.NotificationListResponse;
import de.fallstudie.minerva.backend.notification.internal.web.NotificationResponse;
import de.fallstudie.minerva.backend.common.ResourceNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {
	private final NotificationRepository notificationRepository;
	private final ObjectMapper objectMapper;

	@Transactional
	public void saveAll(List<Notification> notifications) {
		if (notifications.isEmpty()) {
			return;
		}

		final var createdAt = Instant.now();
		notificationRepository.saveAll(notifications.stream()
				.map(notification -> toModel(notification, createdAt)).toList());
	}

	public NotificationListResponse getAll(long recipientUserId) {
		final var notifications = notificationRepository
				.findAllByRecipientUserIdOrderByCreatedAtDescIdDesc(recipientUserId).stream()
				.map(this::toResponse).toList();
		return new NotificationListResponse(notifications);
	}

	@Transactional
	public void markAsRead(long recipientUserId, long notificationId) {
		final var notification = notificationRepository
				.findByIdAndRecipientUserId(notificationId, recipientUserId).orElseThrow(
						() -> new ResourceNotFoundException("Benachrichtigung nicht gefunden"));
		if (notification.getReadAt() == null) {
			notification.setReadAt(Instant.now());
			notificationRepository.save(notification);
		}
	}

	private NotificationModel toModel(Notification notification, Instant createdAt) {
		final var model = new NotificationModel();
		model.setRecipientUserId(notification.recipientUserId());
		model.setType(typeOf(notification));
		model.setPayloadJson(toJson(notification));
		model.setCreatedAt(createdAt);
		return model;
	}

	private NotificationType typeOf(Notification notification) {
		return switch (notification) {
			case Notification.TicketAssigned _ -> NotificationType.TICKET_ASSIGNED;
			case Notification.TicketUnassigned _ -> NotificationType.TICKET_UNASSIGNED;
			case Notification.TicketCommentCreated _ -> NotificationType.TICKET_COMMENT_CREATED;
			case Notification.TicketStatusChanged _ -> NotificationType.TICKET_STATUS_CHANGED;
			case Notification.TicketDetailsUpdated _ -> NotificationType.TICKET_DETAILS_UPDATED;
			case Notification.TicketPriorityChanged _ -> NotificationType.TICKET_PRIORITY_CHANGED;
			case Notification.TicketSubticketAdded _ -> NotificationType.TICKET_SUBTICKET_ADDED;
			case Notification.TicketArchived _ -> NotificationType.TICKET_ARCHIVED;
			case Notification.TicketRestored _ -> NotificationType.TICKET_RESTORED;
			case Notification.TicketDeleted _ -> NotificationType.TICKET_DELETED;
			case Notification.ProjectUserAdded _ -> NotificationType.PROJECT_USER_ADDED;
			case Notification.ProjectUserRoleChanged _ ->
				NotificationType.PROJECT_USER_ROLE_CHANGED;
			case Notification.ProjectUserRemoved _ -> NotificationType.PROJECT_USER_REMOVED;
			case Notification.UserCreated _ -> NotificationType.USER_CREATED;
			case Notification.UserUsernameChanged _ -> NotificationType.USER_USERNAME_CHANGED;
			case Notification.UserPasswordChanged _ -> NotificationType.USER_PASSWORD_CHANGED;
			case Notification.UserWorkspaceRoleChanged _ ->
				NotificationType.USER_WORKSPACE_ROLE_CHANGED;
			case Notification.UserDeactivated _ -> NotificationType.USER_DEACTIVATED;
			case Notification.UserReactivated _ -> NotificationType.USER_REACTIVATED;
		};
	}

	private String toJson(Notification notification) {
		try {
			return objectMapper.writeValueAsString(notification);
		} catch (JacksonException exception) {
			throw new IllegalStateException("Could not serialize notification payload", exception);
		}
	}

	private NotificationResponse toResponse(NotificationModel notification) {
		try {
			return new NotificationResponse(notification.getId(), notification.getType(),
					objectMapper.readTree(notification.getPayloadJson()),
					notification.getCreatedAt(), notification.getReadAt());
		} catch (JacksonException exception) {
			throw new IllegalStateException("Could not deserialize notification payload",
					exception);
		}
	}
}
