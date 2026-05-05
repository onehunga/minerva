package de.fallstudie.minerva.backend.auth;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshTokenModel, Long> {
	Optional<RefreshTokenModel> findByTokenHash(String tokenHash);

	void deleteByExpiresAtBefore(Instant expiresAt);

	@Modifying
	@Query("delete from RefreshTokenModel refreshToken where refreshToken.tokenHash = :tokenHash")
	void deleteByTokenHash(@Param("tokenHash") String tokenHash);
}
