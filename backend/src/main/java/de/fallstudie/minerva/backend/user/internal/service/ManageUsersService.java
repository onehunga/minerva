package de.fallstudie.minerva.backend.user.internal.service;

import de.fallstudie.minerva.backend.authorization.WorkspaceRoleName;
import de.fallstudie.minerva.backend.authorization.WorkspaceRoleService;
import de.fallstudie.minerva.backend.common.DuplicateResourceException;
import de.fallstudie.minerva.backend.common.ValidationException;
import de.fallstudie.minerva.backend.user.UserModel;
import de.fallstudie.minerva.backend.user.UserRepository;
import de.fallstudie.minerva.backend.user.internal.web.UserRecordListResponse;
import de.fallstudie.minerva.backend.user.internal.web.UserRecordResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ManageUsersService {
	private static final int MIN_USERNAME_LENGTH = 3;
	private static final int MAX_USERNAME_LENGTH = 50;
	private static final int MIN_PASSWORD_LENGTH = 8;
	private static final String USERNAME_PATTERN = "[A-Za-z0-9._-]+";

	private final PasswordEncoder passwordEncoder;
	private final WorkspaceRoleService workspaceRoleService;
	private final UserRepository userRepository;

	@Transactional
	public void createUser(String username, String password, String workspaceRole) {
		final var validatedUsername = validateUsername(username);
		validatePassword(password);
		final var workspaceRoleName = validateWorkspaceRole(workspaceRole);

		if (userRepository.existsByUsername(validatedUsername)) {
			throw new DuplicateResourceException("Username already exists");
		}

		final var role = workspaceRoleService.find(workspaceRoleName)
				.orElseThrow(() -> new ValidationException("Workspace role does not exist"));

		final var user = new UserModel();
		user.setUsername(validatedUsername);
		user.setPassword(passwordEncoder.encode(password));

		user.setWorkspaceRole(role);

		userRepository.save(user);
		userRepository.flush();
	}

	public UserRecordListResponse getAllUsers() {
		final var users = userRepository.findAll().stream()
				.map(user -> new UserRecordResponse(user.getId(), user.getUsername(),
						user.getWorkspaceRole().getName()))
				.toList();

		return new UserRecordListResponse(users);
	}

	private String validateUsername(String username) {
		if (username == null || username.isBlank()) {
			throw new ValidationException("Username is required");
		}

		final var trimmedUsername = username.trim();

		if (trimmedUsername.length() < MIN_USERNAME_LENGTH
				|| trimmedUsername.length() > MAX_USERNAME_LENGTH) {
			throw new ValidationException("Username must be between 3 and 50 characters");
		}

		if (!trimmedUsername.matches(USERNAME_PATTERN)) {
			throw new ValidationException(
					"Username may only contain letters, numbers, '.', '_' or '-'");
		}

		return trimmedUsername;
	}

	private void validatePassword(String password) {
		if (password == null || password.isBlank()) {
			throw new ValidationException("Password is required");
		}

		if (password.length() < MIN_PASSWORD_LENGTH) {
			throw new ValidationException("Password must be at least 8 characters");
		}
	}

	private WorkspaceRoleName validateWorkspaceRole(String workspaceRole) {
		if (workspaceRole == null || workspaceRole.isBlank()) {
			throw new ValidationException("Role is required");
		}

		try {
			return WorkspaceRoleName.valueOf(workspaceRole.trim());
		} catch (IllegalArgumentException exception) {
			throw new ValidationException("Role must be ADMIN or USER");
		}
	}
}
