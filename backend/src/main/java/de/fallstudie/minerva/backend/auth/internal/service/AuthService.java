package de.fallstudie.minerva.backend.auth.internal.service;

import de.fallstudie.minerva.backend.auth.internal.AuthenticationFailedException;
import de.fallstudie.minerva.backend.auth.internal.persistence.RefreshTokenModel;
import de.fallstudie.minerva.backend.auth.internal.persistence.RefreshTokenRepository;
import de.fallstudie.minerva.backend.auth.internal.utils.RequestUtils;
import de.fallstudie.minerva.backend.auth.internal.web.LoginRequest;
import de.fallstudie.minerva.backend.auth.internal.web.RefreshTokenRequest;
import de.fallstudie.minerva.backend.auth.internal.web.TokenResponse;
import de.fallstudie.minerva.backend.user.UserDTO;
import de.fallstudie.minerva.backend.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;

@Service
@RequiredArgsConstructor
public class AuthService {
	private static final SecureRandom SECURE_RANDOM = new SecureRandom();
	private static final int REFRESH_TOKEN_BYTES = 64;

	private final UserService userService;
	private final RefreshTokenRepository refreshTokenRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtService jwtService;

	@Value("${spring.security.jwt.refresh-expiration}")
	private long refreshExpirationMillis;

	@Transactional
	public TokenResponse login(LoginRequest request) {
		String username = RequestUtils.requireValue(request.username(),
				"Benutzername ist erforderlich");
		String password = RequestUtils.requireValue(request.password(),
				"Passwort ist erforderlich");

		UserDTO user = userService.findByUsername(username).orElseThrow(
				() -> new AuthenticationFailedException("Benutzername oder Passwort ist ungültig"));

		if (!passwordEncoder.matches(password, user.passwordHash())) {
			throw new AuthenticationFailedException("Benutzername oder Passwort ist ungültig");
		}

		return createAuthResponse(user);
	}

	@Transactional(noRollbackFor = AuthenticationFailedException.class)
	public TokenResponse refresh(RefreshTokenRequest request) {
		String refreshToken = RequestUtils.requireValue(request.refreshToken(),
				"Refresh-Token ist erforderlich");
		String tokenHash = hash(refreshToken);

		RefreshTokenModel storedToken = refreshTokenRepository.findByTokenHash(tokenHash)
				.orElseThrow(() -> new AuthenticationFailedException("Refresh-Token ist ungültig"));

		if (storedToken.getExpiresAt().isBefore(Instant.now())) {
			refreshTokenRepository.deleteByTokenHash(tokenHash);
			refreshTokenRepository.flush();
			throw new AuthenticationFailedException("Refresh-Token ist abgelaufen");
		}

		UserDTO user = userService.findActiveById(storedToken.getUserId())
				.orElseThrow(() -> new AuthenticationFailedException("Refresh-Token ist ungültig"));
		refreshTokenRepository.deleteByTokenHash(tokenHash);
		refreshTokenRepository.flush();

		return createAuthResponse(user);
	}

	@Transactional
	public void deleteExpiredRefreshTokens() {
		refreshTokenRepository.deleteByExpiresAtBefore(Instant.now());
	}

	/// Widerruft nur die durch den übergebenen Refresh-Token identifizierte Sitzung. Andere
	/// Sitzungen bleiben aktiv.
	@Transactional
	public void logout(RefreshTokenRequest request) {
		String refreshToken = RequestUtils.requireValue(request.refreshToken(),
				"Refresh-Token ist erforderlich");
		refreshTokenRepository.deleteByTokenHash(hash(refreshToken));
	}

	private TokenResponse createAuthResponse(UserDTO user) {
		String accessToken = jwtService.generateToken(user.id());
		String refreshToken = generateRefreshToken();

		RefreshTokenModel refreshTokenModel = new RefreshTokenModel();
		refreshTokenModel.setUserId(user.id());
		refreshTokenModel.setTokenHash(hash(refreshToken));
		refreshTokenModel
				.setExpiresAt(Instant.now().plus(Duration.ofMillis(refreshExpirationMillis)));
		refreshTokenRepository.save(refreshTokenModel);

		return new TokenResponse(accessToken, refreshToken);
	}

	/// Generate a Random Refresh Token and return its String representation.
	private String generateRefreshToken() {
		byte[] bytes = new byte[REFRESH_TOKEN_BYTES];
		SECURE_RANDOM.nextBytes(bytes);
		return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
	}

	/// General Hash function
	private String hash(String value) {
		try {
			byte[] bytes = MessageDigest.getInstance("SHA-256")
					.digest(value.getBytes(StandardCharsets.UTF_8));
			return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
		} catch (NoSuchAlgorithmException exception) {
			throw new IllegalStateException("SHA-256 is not available", exception);
		}
	}
}
