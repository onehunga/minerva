package de.fallstudie.minerva.backend.notification.internal.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<NotificationModel, Long> {
}
