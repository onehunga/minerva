package de.fallstudie.minerva.backend.auth.internal.service;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;

import de.fallstudie.minerva.backend.auth.internal.persistence.RefreshTokenRepository;
import de.fallstudie.minerva.backend.user.UserEvent;

class AuthUserDeletionListenerTests {
	@Test
	void deletesAllRefreshTokensForDeletedUser() {
		final var repository = mock(RefreshTokenRepository.class);
		final var listener = new AuthUserDeletionListener(repository);

		listener.on(new UserEvent.Deleted(1L, 2L));

		verify(repository).deleteByUserId(2L);
	}

	@Test
	void deletesAllRefreshTokensForDeactivatedUser() {
		final var repository = mock(RefreshTokenRepository.class);
		final var listener = new AuthUserDeletionListener(repository);

		listener.on(new UserEvent.Deactivated(1L, 2L));

		verify(repository).deleteByUserId(2L);
	}
}
