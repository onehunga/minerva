package de.fallstudie.minerva.backend.user;

import de.fallstudie.minerva.backend.auth.Identity;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/users")
@RequiredArgsConstructor
public class UserController {
	private final UserService userService;

	@GetMapping("/me")
	public UserDetailsResponse me(@AuthenticationPrincipal Identity identity) {
		return userService.getUserDetails(identity.userId());
	}
}
