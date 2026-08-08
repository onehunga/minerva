package de.fallstudie.minerva.backend.notification.internal.service;

import de.fallstudie.minerva.backend.notification.internal.Notification;
import de.fallstudie.minerva.backend.project.ProjectEvent;
import de.fallstudie.minerva.backend.realtime.UserEventStream;
import de.fallstudie.minerva.backend.ticket.TicketEvent;
import de.fallstudie.minerva.backend.user.UserEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.function.LongFunction;

@Component
@RequiredArgsConstructor
public class NotificationEventHandler {
	private final NotificationService notificationService;
	private final UserEventStream userEventStream;

	@Async
	@TransactionalEventListener
	public void on(TicketEvent event) {
		publish(notificationService.saveAll(map(event)));
	}

	@Async
	@TransactionalEventListener
	public void on(ProjectEvent event) {
		publish(notificationService.saveAll(map(event)));
	}

	@Async
	@TransactionalEventListener
	public void on(UserEvent event) {
		publish(notificationService.saveAll(map(event)));
	}

	private void publish(List<Long> recipientUserIds) {
		// saveAll has completed its transaction before this asynchronous listener
		// sends live data. The REST endpoint can therefore always retrieve the
		// notifications a client is asked to refetch.
		recipientUserIds.stream().distinct().forEach(
				recipientUserId -> userEventStream.invalidate(recipientUserId, "notifications"));
	}

	private List<Notification> map(TicketEvent event) {
		return switch (event) {
			case TicketEvent.TicketCreated e -> forRecipient(e.assigneeId(), e.actorUserId(),
					recipient -> new Notification.TicketAssigned(recipient, e.actorUserId(),
							e.projectId(), e.ticketId(), null, e.assigneeId()));
			case TicketEvent.CommentCreated e -> forRecipient(e.assigneeId(), e.actorUserId(),
					recipient -> new Notification.TicketCommentCreated(recipient, e.actorUserId(),
							e.projectId(), e.ticketId(), e.commentId(), e.content()));
			case TicketEvent.StatusChanged e -> forRecipient(e.assigneeId(), e.actorUserId(),
					recipient -> new Notification.TicketStatusChanged(recipient, e.actorUserId(),
							e.projectId(), e.ticketId(), e.previousStatusId(),
							e.previousStatusName(), e.newStatusId(), e.newStatusName(),
							e.transitionId(), e.transitionName()));
			case TicketEvent.DetailsUpdated e -> forRecipient(e.assigneeId(), e.actorUserId(),
					recipient -> new Notification.TicketDetailsUpdated(recipient, e.actorUserId(),
							e.projectId(), e.ticketId(), e.previousName(), e.newName(),
							e.previousDescription(), e.newDescription()));
			case TicketEvent.PriorityChanged e -> forRecipient(e.assigneeId(), e.actorUserId(),
					recipient -> new Notification.TicketPriorityChanged(recipient, e.actorUserId(),
							e.projectId(), e.ticketId(), e.previousPriority().name(),
							e.newPriority().name()));
			case TicketEvent.AssigneeChanged e -> map(e);
			case TicketEvent.SubticketAdded e -> forRecipient(e.assigneeId(), e.actorUserId(),
					recipient -> new Notification.TicketSubticketAdded(recipient, e.actorUserId(),
							e.projectId(), e.ticketId(), e.subticketId(), e.subticketName()));
			case TicketEvent.TicketArchived e -> forRecipient(e.assigneeId(), e.actorUserId(),
					recipient -> new Notification.TicketArchived(recipient, e.actorUserId(),
							e.projectId(), e.ticketId(), e.name()));
			case TicketEvent.TicketRestored e -> forRecipient(e.assigneeId(), e.actorUserId(),
					recipient -> new Notification.TicketRestored(recipient, e.actorUserId(),
							e.projectId(), e.ticketId(), e.name()));
			case TicketEvent.TicketDeleted e -> forRecipient(e.assigneeId(), e.actorUserId(),
					recipient -> new Notification.TicketDeleted(recipient, e.actorUserId(),
							e.projectId(), e.ticketId(), e.name()));
		};
	}

	private List<Notification> map(TicketEvent.AssigneeChanged event) {
		final var notifications = new ArrayList<Notification>(2);
		if (event.previousAssigneeId() != null
				&& event.previousAssigneeId() != event.actorUserId()) {
			notifications.add(new Notification.TicketUnassigned(event.previousAssigneeId(),
					event.actorUserId(), event.projectId(), event.ticketId(),
					event.previousAssigneeId(), event.newAssigneeId()));
		}
		if (event.newAssigneeId() != null && event.newAssigneeId() != event.actorUserId()) {
			notifications.add(new Notification.TicketAssigned(event.newAssigneeId(),
					event.actorUserId(), event.projectId(), event.ticketId(),
					event.previousAssigneeId(), event.newAssigneeId()));
		}
		return notifications;
	}

	private List<Notification> map(ProjectEvent event) {
		return switch (event) {
			case ProjectEvent.UserAdded e -> forRecipient(e.userId(), e.actorUserId(),
					recipient -> new Notification.ProjectUserAdded(recipient, e.actorUserId(),
							e.projectId(), e.role()));
			case ProjectEvent.UserRoleChanged e -> forRecipient(e.userId(), e.actorUserId(),
					recipient -> new Notification.ProjectUserRoleChanged(recipient, e.actorUserId(),
							e.projectId(), e.oldRole(), e.newRole()));
			case ProjectEvent.UserRemoved e -> forRecipient(e.userId(), e.actorUserId(),
					recipient -> new Notification.ProjectUserRemoved(recipient, e.actorUserId(),
							e.projectId()));
			case ProjectEvent.ProjectCreated _,ProjectEvent.DetailsUpdated _,ProjectEvent.ProjectArchived _,ProjectEvent.ProjectRestored _ ->
				List.of();
		};
	}

	private List<Notification> map(UserEvent event) {
		return switch (event) {
			case UserEvent.Created e -> List.of(new Notification.UserCreated(e.userId(),
					e.actorUserId(), e.username(), e.role().name()));
			case UserEvent.UsernameChanged e ->
				List.of(new Notification.UserUsernameChanged(e.userId(), e.actorUserId(),
						e.previousUsername(), e.newUsername()));
			case UserEvent.PasswordChanged e ->
				List.of(new Notification.UserPasswordChanged(e.userId(), e.actorUserId()));
			case UserEvent.WorkspaceRoleChanged e ->
				List.of(new Notification.UserWorkspaceRoleChanged(e.userId(), e.actorUserId(),
						e.oldRole().name(), e.newRole().name()));
			case UserEvent.Deactivated e ->
				List.of(new Notification.UserDeactivated(e.userId(), e.actorUserId()));
			case UserEvent.Reactivated e ->
				List.of(new Notification.UserReactivated(e.userId(), e.actorUserId()));
			case UserEvent.Deleted _ -> List.of();
		};
	}

	private List<Notification> forRecipient(Long recipientUserId, long actorUserId,
			LongFunction<Notification> factory) {
		return recipientUserId == null || recipientUserId == actorUserId
				? List.of()
				: List.of(factory.apply(recipientUserId));
	}
}
