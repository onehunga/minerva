package de.fallstudie.minerva.backend.user.internal.web;

import de.fallstudie.minerva.backend.auth.Identity;
import de.fallstudie.minerva.backend.authorization.AuthorizationException;
import de.fallstudie.minerva.backend.authorization.WorkspaceAuthorizationService;
import de.fallstudie.minerva.backend.user.internal.service.ManageUsersService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/v1/users")
@RequiredArgsConstructor
public class ManageUserController {
	private final ManageUsersService userService;
	private final WorkspaceAuthorizationService workspaceAuthorizationService;

	@PostMapping("/create")
	@ResponseStatus(HttpStatus.CREATED)
	public void create(@AuthenticationPrincipal Identity identity,
			@RequestBody CreateUserRequest createUserRequest) throws AuthorizationException {
		log.trace("Creating user with username {} and workspace role {}",
				createUserRequest.username(), createUserRequest.role());

		workspaceAuthorizationService.isWorkspaceAdmin(identity.userId());
		userService.createUser(createUserRequest.username(), createUserRequest.password(),
				createUserRequest.role());
	}

	@GetMapping("/allUsers")
	public UserRecordListResponse get(@AuthenticationPrincipal Identity identity)
			throws AuthorizationException {
		log.trace("Getting all users for workspace");

		workspaceAuthorizationService.isWorkspaceAdmin(identity.userId());
		return userService.getAllUsers();
	}
}
