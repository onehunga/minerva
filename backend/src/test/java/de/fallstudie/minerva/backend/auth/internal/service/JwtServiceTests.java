package de.fallstudie.minerva.backend.auth.internal.service;

import de.fallstudie.minerva.backend.user.UserService;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class JwtServiceTests {
	private static final long USER_ID = 42L;

	private UserService userService;
	private JwtService jwtService;

	@BeforeEach
	void setUp() {
		userService = mock(UserService.class);
		jwtService = new JwtService(userService);
		ReflectionTestUtils.setField(jwtService, "jwtSecret", "test-secret");
		ReflectionTestUtils.setField(jwtService, "jwtExpirationMillis", 60_000L);
	}

	@Test
	void generatedTokenValidatesToIdentity() {
		when(userService.existsById(USER_ID)).thenReturn(true);

		final var token = jwtService.generateToken(USER_ID);
		final var identity = jwtService.validateIdentity(token);

		assertEquals(USER_ID, identity.userId());
		assertEquals("42", identity.getName());
		verify(userService).existsById(USER_ID);
	}

	@Test
	void validateIdentityRejectsUnknownUser() {
		when(userService.existsById(USER_ID)).thenReturn(false);

		final var token = jwtService.generateToken(USER_ID);

		assertThrows(JwtException.class, () -> jwtService.validateIdentity(token));
	}
}
