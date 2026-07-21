package de.fallstudie.minerva.backend.auth.internal.service;

import de.fallstudie.minerva.backend.auth.internal.AuthenticationFailedException;
import de.fallstudie.minerva.backend.auth.internal.persistence.RefreshTokenModel;
import de.fallstudie.minerva.backend.auth.internal.persistence.RefreshTokenRepository;
import de.fallstudie.minerva.backend.auth.internal.web.LoginRequest;
import de.fallstudie.minerva.backend.auth.internal.web.RefreshTokenRequest;
import de.fallstudie.minerva.backend.user.UserDTO;
import de.fallstudie.minerva.backend.user.UserService;
import de.fallstudie.minerva.backend.user.WorkspaceRoleName;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Base64;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthServiceTests {
	private static final long USER_ID = 42L;
	private static final UserDTO USER = new UserDTO(USER_ID, "jane", "password-hash",
			WorkspaceRoleName.USER, false);

	private UserService userService;
	private RefreshTokenRepository refreshTokenRepository;
	private PasswordEncoder passwordEncoder;
	private JwtService jwtService;
	private AuthService authService;

	@BeforeEach
	void setUp() {
		userService = mock(UserService.class);
		refreshTokenRepository = mock(RefreshTokenRepository.class);
		passwordEncoder = mock(PasswordEncoder.class);
		jwtService = mock(JwtService.class);
		authService = new AuthService(userService, refreshTokenRepository, passwordEncoder,
				jwtService);
		ReflectionTestUtils.setField(authService, "refreshExpirationMillis", 60_000L);
	}

	@Test
	void loginUsesPublicUserServiceAndStoresUserIdOnRefreshToken() {
		when(userService.findByUsername("jane")).thenReturn(Optional.of(USER));
		when(passwordEncoder.matches("password", "password-hash")).thenReturn(true);
		when(jwtService.generateToken(USER_ID)).thenReturn("access-token");

		final var response = authService.login(new LoginRequest("jane", "password"));

		assertEquals("access-token", response.accessToken());
		assertFalse(response.refreshToken().isBlank());

		ArgumentCaptor<RefreshTokenModel> refreshTokenCaptor = ArgumentCaptor
				.forClass(RefreshTokenModel.class);
		verify(refreshTokenRepository).save(refreshTokenCaptor.capture());
		assertEquals(USER_ID, refreshTokenCaptor.getValue().getUserId());
		assertNotNull(refreshTokenCaptor.getValue().getTokenHash());
		assertNotNull(refreshTokenCaptor.getValue().getExpiresAt());
	}

	@Test
	void loginRejectsInvalidPassword() {
		when(userService.findByUsername("jane")).thenReturn(Optional.of(USER));
		when(passwordEncoder.matches("password", "password-hash")).thenReturn(false);

		assertThrows(AuthenticationFailedException.class,
				() -> authService.login(new LoginRequest("jane", "password")));

		verify(refreshTokenRepository, never()).save(any());
	}

	@Test
	void refreshRotatesTokenAndLoadsUserThroughPublicUserService() {
		final var refreshToken = "refresh-token";
		final var refreshTokenHash = hash(refreshToken);
		final var storedToken = new RefreshTokenModel();
		storedToken.setUserId(USER_ID);
		storedToken.setTokenHash(refreshTokenHash);
		storedToken.setExpiresAt(Instant.now().plusSeconds(60));

		when(refreshTokenRepository.findByTokenHash(refreshTokenHash))
				.thenReturn(Optional.of(storedToken));
		when(userService.findActiveById(USER_ID)).thenReturn(Optional.of(USER));
		when(jwtService.generateToken(USER_ID)).thenReturn("access-token");

		final var response = authService.refresh(new RefreshTokenRequest(refreshToken));

		assertEquals("access-token", response.accessToken());
		assertFalse(response.refreshToken().isBlank());
		verify(refreshTokenRepository).deleteByTokenHash(refreshTokenHash);
		verify(refreshTokenRepository).flush();
		verify(userService).findActiveById(USER_ID);
	}

	@Test
	void logoutRevokesRefreshToken() {
		authService.logout(new RefreshTokenRequest("refresh-token"));

		verify(refreshTokenRepository).deleteByTokenHash(hash("refresh-token"));
	}

	private static String hash(String value) {
		try {
			byte[] bytes = MessageDigest.getInstance("SHA-256")
					.digest(value.getBytes(StandardCharsets.UTF_8));
			return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
		} catch (NoSuchAlgorithmException exception) {
			throw new IllegalStateException("SHA-256 is not available", exception);
		}
	}
}
