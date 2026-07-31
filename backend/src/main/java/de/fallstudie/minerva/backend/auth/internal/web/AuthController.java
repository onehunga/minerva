package de.fallstudie.minerva.backend.auth.internal.web;

import de.fallstudie.minerva.backend.auth.internal.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/auth")
@RequiredArgsConstructor
public class AuthController {
	private final AuthService authService;

	@PostMapping("/login")
	public TokenResponse login(@RequestBody LoginRequest request) {
		return authService.login(request);
	}

	@PostMapping("/refresh")
	public TokenResponse refresh(@RequestBody RefreshTokenRequest request) {
		return authService.refresh(request);
	}

	@PostMapping("/logout")
	public void logout(@RequestBody RefreshTokenRequest request) {
		authService.logout(request);
	}
}
