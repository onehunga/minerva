package de.fallstudie.minerva.backend.auth;

import de.fallstudie.minerva.backend.user.UserModel;
import de.fallstudie.minerva.backend.user.UserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Date;

@Service
@RequiredArgsConstructor
public class JwtService {
	private static final String HMAC_SHA256 = "HmacSHA256";

	private final UserRepository userRepository;

	@Value("${spring.security.jwt.secret}")
	private String jwtSecret;

	@Value("${spring.security.jwt.expiration}")
	private long jwtExpirationMillis;

	public String generateToken(UserModel user) {
		Instant now = Instant.now();
		Instant expiresAt = now.plusMillis(jwtExpirationMillis);

		return Jwts.builder().subject(user.getUsername()).issuedAt(Date.from(now))
				.expiration(Date.from(expiresAt)).signWith(signingKey()).compact();
	}

	public Identity validateIdentity(String token) {
		final Claims claims = parseClaims(token);

		final String username = claims.getSubject();

		if (username == null) {
			throw new JwtException("Username is null");
		}

		final UserModel model = this.userRepository.findByUsername(username)
				.orElseThrow(() -> new JwtException("User not found"));

		return new Identity(model.getId());
	}

	private Claims parseClaims(String token) {
		return Jwts.parser().verifyWith(signingKey()).build().parseSignedClaims(token).getPayload();
	}

	private SecretKey signingKey() {
		return new SecretKeySpec(sha256(jwtSecret), HMAC_SHA256);
	}

	private byte[] sha256(String value) {
		try {
			return MessageDigest.getInstance("SHA-256")
					.digest(value.getBytes(StandardCharsets.UTF_8));
		} catch (NoSuchAlgorithmException exception) {
			throw new IllegalStateException("SHA-256 is not available", exception);
		}
	}
}
