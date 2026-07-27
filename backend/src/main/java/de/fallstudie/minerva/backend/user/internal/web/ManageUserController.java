package de.fallstudie.minerva.backend.user.internal.web;

import de.fallstudie.minerva.backend.user.internal.service.ManageUsersService;
import de.fallstudie.minerva.backend.user.Identity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/v1/users")
@RequiredArgsConstructor
public class ManageUserController {
	private final ManageUsersService userService;

	@GetMapping
	@PreAuthorize("isAuthenticated()")
	public UserRecordListResponse get() {
		log.trace("Getting all users for workspace");

		return userService.getAllUsers();
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	@PreAuthorize("@userPolicies.isAdmin(principal)")
	public void create(@RequestBody CreateUserRequest createUserRequest) {
		log.trace("Creating user with username {} and workspace role {}",
				createUserRequest.username(), createUserRequest.role());

		userService.createUser(createUserRequest.username(), createUserRequest.password(),
				createUserRequest.role());
	}

	@PatchMapping("/{userId}/role")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@PreAuthorize("@userPolicies.isAdmin(principal)")
	public void updateRole(@PathVariable long userId,
			@RequestBody UpdateUserRoleRequest updateUserRoleRequest) {
		log.trace("Updating user with id {} to workspace role {}", userId,
				updateUserRoleRequest.role());

		userService.updateUserRole(userId, updateUserRoleRequest.role());
	}

	@PatchMapping("/{userId}/username")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@PreAuthorize("@userPolicies.isAdminOrSelf(principal, #userId)")
	public void updateUsername(@PathVariable long userId,
			@RequestBody UpdateUsernameRequest updateUsernameRequest) {
		log.trace("Updating username for user with id {}", userId);

		userService.updateUsername(userId, updateUsernameRequest.username());
	}

	@PatchMapping("/{userId}/password")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@PreAuthorize("@userPolicies.isAdminOrSelf(principal, #userId)")
	public void updatePassword(@PathVariable long userId,
			@RequestBody UpdateUserPasswordRequest updateUserPasswordRequest) {
		log.trace("Updating password for user with id {}", userId);

		userService.updatePassword(userId, updateUserPasswordRequest.password());
	}

	@DeleteMapping("/{userId}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@PreAuthorize("@userPolicies.isAdmin(principal)")
	public void delete(@AuthenticationPrincipal Identity identity, @PathVariable long userId) {
		log.trace("Deleting user with id {}", userId);

		userService.deleteUser(identity.userId(), userId);
	}
}
