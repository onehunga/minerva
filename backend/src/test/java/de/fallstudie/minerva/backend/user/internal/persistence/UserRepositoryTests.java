package de.fallstudie.minerva.backend.user.internal.persistence;

import de.fallstudie.minerva.backend.auth.internal.persistence.RefreshTokenModel;
import de.fallstudie.minerva.backend.auth.internal.persistence.RefreshTokenRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DataJpaTest(properties = {"spring.jpa.hibernate.ddl-auto=create-drop",
		"spring.liquibase.enabled=false"})
@ActiveProfiles("test")
class UserRepositoryTests {
	@Autowired
	private UserRepository userRepository;

	@Autowired
	private RefreshTokenRepository refreshTokenRepository;

	@Test
	void saveAssignsEmbeddedUserId() {
		final var user = new UserModel();
		user.setUsername("jane");
		user.setPassword("password-hash");

		final var savedUser = userRepository.saveAndFlush(user);

		assertNotNull(savedUser.getId());
		assertEquals(1L, savedUser.getId());
	}

	@Test
	void refreshTokenPersistsEmbeddedUserId() {
		final var user = new UserModel();
		user.setUsername("jane");
		user.setPassword("password-hash");
		final var savedUser = userRepository.saveAndFlush(user);

		final var refreshToken = new RefreshTokenModel();
		refreshToken.setUserId(savedUser.getId());
		refreshToken.setTokenHash("token-hash");
		refreshToken.setExpiresAt(Instant.now().plusSeconds(60));

		final var savedToken = refreshTokenRepository.saveAndFlush(refreshToken);

		assertEquals(savedUser.getId(), savedToken.getUserId());
	}
}
