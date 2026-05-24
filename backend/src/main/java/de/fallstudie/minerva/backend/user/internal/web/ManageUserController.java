package de.fallstudie.minerva.backend.user.internal.web;

import de.fallstudie.minerva.backend.authorization.AuthorizationException;
import de.fallstudie.minerva.backend.authorization.WorkspaceAuthorizationService;
import de.fallstudie.minerva.backend.user.Identity;
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

	@GetMapping
	public UserRecordListResponse get(@AuthenticationPrincipal Identity identity)
			throws AuthorizationException {
		log.trace("Getting all users for workspace");

		workspaceAuthorizationService.isWorkspaceAdmin(identity.userId());
		return userService.getAllUsers();
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public void create(@AuthenticationPrincipal Identity identity,
			@RequestBody CreateUserRequest createUserRequest) throws AuthorizationException {
		log.trace("Creating user with username {} and workspace role {}",
				createUserRequest.username(), createUserRequest.role());

		workspaceAuthorizationService.isWorkspaceAdmin(identity.userId());
		userService.createUser(createUserRequest.username(), createUserRequest.password(),
				createUserRequest.role());
	}

	@PatchMapping("/{userId}/role")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void updateRole(@AuthenticationPrincipal Identity identity, @PathVariable long userId,
			@RequestBody UpdateUserRoleRequest updateUserRoleRequest)
			throws AuthorizationException {
		log.trace("Updating user with id {} to workspace role {}", userId,
				updateUserRoleRequest.role());

		workspaceAuthorizationService.isWorkspaceAdmin(identity.userId());
		userService.updateUserRole(userId, updateUserRoleRequest.role());
	}

	@PatchMapping("/{userId}/username")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void updateUsername(@AuthenticationPrincipal Identity identity,
			@PathVariable long userId, @RequestBody UpdateUsernameRequest updateUsernameRequest)
			throws AuthorizationException {
		log.trace("Updating username for user with id {}", userId);

		workspaceAuthorizationService.isWorkspaceAdmin(identity.userId());
		userService.updateUsername(userId, updateUsernameRequest.username());
	}

	@PatchMapping("/{userId}/password")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void updatePassword(@AuthenticationPrincipal Identity identity,
			@PathVariable long userId,
			@RequestBody UpdateUserPasswordRequest updateUserPasswordRequest)
			throws AuthorizationException {
		log.trace("Updating password for user with id {}", userId);

		workspaceAuthorizationService.isWorkspaceAdmin(identity.userId());
		userService.updatePassword(userId, updateUserPasswordRequest.password());
	}

	@DeleteMapping("/{userId}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void delete(@AuthenticationPrincipal Identity identity, @PathVariable long userId)
			throws AuthorizationException {
		log.trace("Deleting user with id {}", userId);

		workspaceAuthorizationService.isWorkspaceAdmin(identity.userId());
		userService.deleteUser(userId);
	}
}
