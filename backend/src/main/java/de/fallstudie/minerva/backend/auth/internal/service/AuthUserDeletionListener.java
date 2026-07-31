package de.fallstudie.minerva.backend.auth.internal.service;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import de.fallstudie.minerva.backend.auth.internal.persistence.RefreshTokenRepository;
import de.fallstudie.minerva.backend.user.UserEvent;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class AuthUserDeletionListener {
	private final RefreshTokenRepository refreshTokenRepository;

	@EventListener
	public void on(UserEvent.Deleted event) {
		refreshTokenRepository.deleteByUserId(event.userId());
	}

	@EventListener
	public void on(UserEvent.Deactivated event) {
		refreshTokenRepository.deleteByUserId(event.userId());
	}
}
