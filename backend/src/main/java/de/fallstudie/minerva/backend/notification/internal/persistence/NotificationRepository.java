package de.fallstudie.minerva.backend.notification.internal.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface NotificationRepository extends JpaRepository<NotificationModel, Long> {
	List<NotificationModel> findAllByRecipientUserIdOrderByCreatedAtDescIdDesc(
			long recipientUserId);

	Optional<NotificationModel> findByIdAndRecipientUserId(long id, long recipientUserId);
}
