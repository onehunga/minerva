package de.fallstudie.minerva.backend.auth;

import de.fallstudie.minerva.backend.user.UserModel;
import de.fallstudie.minerva.backend.user.UserRepository;
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

	private final UserRepository userRepository;
	private final RefreshTokenRepository refreshTokenRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtService jwtService;

	@Value("${spring.security.jwt.refresh-expiration}")
	private long refreshExpirationMillis;

	@Transactional
	public void register(RegisterRequest request) {
		String username = RequestUtils.requireValue(request.username(), "Username is required");
		String password = RequestUtils.requireValue(request.password(), "Password is required");

		if (userRepository.existsByUsername(username)) {
			throw new DuplicateUsernameException("Username already exists");
		}

		UserModel user = new UserModel();
		user.setUsername(username);
		user.setPassword(passwordEncoder.encode(password));
		userRepository.save(user);
	}

	@Transactional
	public TokenResponse login(LoginRequest request) {
		String username = RequestUtils.requireValue(request.username(), "Username is required");
		String password = RequestUtils.requireValue(request.password(), "Password is required");

		UserModel user = userRepository.findByUsername(username).orElseThrow(
				() -> new AuthenticationFailedException("Invalid username or password"));

		if (!passwordEncoder.matches(password, user.getPassword())) {
			throw new AuthenticationFailedException("Invalid username or password");
		}

		return createAuthResponse(user);
	}

	@Transactional(noRollbackFor = AuthenticationFailedException.class)
	public TokenResponse refresh(RefreshTokenRequest request) {
		String refreshToken = RequestUtils.requireValue(request.refreshToken(),
				"Refresh token is required");
		String tokenHash = hash(refreshToken);

		RefreshTokenModel storedToken = refreshTokenRepository.findByTokenHash(tokenHash)
				.orElseThrow(() -> new AuthenticationFailedException("Invalid refresh token"));

		if (storedToken.getExpiresAt().isBefore(Instant.now())) {
			refreshTokenRepository.deleteByTokenHash(tokenHash);
			refreshTokenRepository.flush();
			throw new AuthenticationFailedException("Refresh token expired");
		}

		UserModel user = storedToken.getUser();
		refreshTokenRepository.deleteByTokenHash(tokenHash);
		refreshTokenRepository.flush();

		return createAuthResponse(user);
	}

	@Transactional
	public void deleteExpiredRefreshTokens() {
		refreshTokenRepository.deleteByExpiresAtBefore(Instant.now());
	}

	private TokenResponse createAuthResponse(UserModel user) {
		String accessToken = jwtService.generateToken(user);
		String refreshToken = generateRefreshToken();

		RefreshTokenModel refreshTokenModel = new RefreshTokenModel();
		refreshTokenModel.setUser(user);
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
