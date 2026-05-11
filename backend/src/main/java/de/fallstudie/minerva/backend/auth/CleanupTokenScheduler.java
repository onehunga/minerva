package de.fallstudie.minerva.backend.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class CleanupTokenScheduler {
	private final AuthService authService;

	@Scheduled(fixedRate = 1, timeUnit = TimeUnit.HOURS)
	public void cleanupTokens() {
		authService.deleteExpiredRefreshTokens();
	}
}
