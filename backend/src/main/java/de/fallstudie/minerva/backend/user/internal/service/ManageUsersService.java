package de.fallstudie.minerva.backend.user.internal.service;

import de.fallstudie.minerva.backend.common.DuplicateResourceException;
import de.fallstudie.minerva.backend.common.ResourceNotFoundException;
import de.fallstudie.minerva.backend.common.ValidationException;
import de.fallstudie.minerva.backend.user.WorkspaceRoleName;
import de.fallstudie.minerva.backend.user.WorkspaceRoleService;
import de.fallstudie.minerva.backend.user.internal.persistence.UserModel;
import de.fallstudie.minerva.backend.user.internal.persistence.UserRepository;
import de.fallstudie.minerva.backend.user.internal.web.UserRecordListResponse;
import de.fallstudie.minerva.backend.user.internal.web.UserRecordResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ManageUsersService {
	private static final String ADMIN_USERNAME = "admin";
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
			throw new DuplicateResourceException("Benutzername ist bereits vergeben");
		}

		final var role = workspaceRoleService.find(workspaceRoleName)
				.orElseThrow(() -> new ValidationException("Arbeitsbereichsrolle existiert nicht"));

		final var user = new UserModel();
		user.setUsername(validatedUsername);
		user.setPassword(passwordEncoder.encode(password));

		user.setWorkspaceRole(role);

		userRepository.save(user);
		userRepository.flush();
	}

	@Transactional
	public void updateUserRole(long userId, String workspaceRole) {
		final var workspaceRoleName = validateWorkspaceRole(workspaceRole);
		final var user = userRepository.findById(userId)
				.orElseThrow(() -> new ResourceNotFoundException("Benutzer nicht gefunden"));

		final var currentRoleName = user.getWorkspaceRole().getName();

		if (currentRoleName == workspaceRoleName) {
			return;
		}

		if (currentRoleName == WorkspaceRoleName.ADMIN
				&& workspaceRoleName == WorkspaceRoleName.USER) {
			validateAdminCanBeDemoted(user);
		}

		final var role = workspaceRoleService.find(workspaceRoleName)
				.orElseThrow(() -> new ValidationException("Arbeitsbereichsrolle existiert nicht"));

		user.setWorkspaceRole(role);
		userRepository.save(user);
		userRepository.flush();
	}

	@Transactional
	public void updateUsername(long userId, String username) {
		final var validatedUsername = validateUsername(username);
		final var user = userRepository.findById(userId)
				.orElseThrow(() -> new ResourceNotFoundException("Benutzer nicht gefunden"));

		if (user.getUsername().equals(validatedUsername)) {
			return;
		}

		if (userRepository.existsByUsername(validatedUsername)) {
			throw new DuplicateResourceException("Benutzername ist bereits vergeben");
		}

		user.setUsername(validatedUsername);
		userRepository.save(user);
		userRepository.flush();
	}

	@Transactional
	public void updatePassword(long userId, String password) {
		validatePassword(password);
		final var user = userRepository.findById(userId)
				.orElseThrow(() -> new ResourceNotFoundException("Benutzer nicht gefunden"));

		user.setPassword(passwordEncoder.encode(password));
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

	@Transactional
	public void deleteUser(long userId) {
		final var user = userRepository.findById(userId)
				.orElseThrow(() -> new ResourceNotFoundException("Benutzer nicht gefunden"));

		userRepository.delete(user);
		userRepository.flush();
	}

	private void validateAdminCanBeDemoted(UserModel user) {
		if (ADMIN_USERNAME.equals(user.getUsername())) {
			throw new ValidationException(
					"Der initiale Admin-Benutzer darf nicht herabgestuft werden");
		}

		if (userRepository.countByWorkspaceRole_Name(WorkspaceRoleName.ADMIN) <= 1) {
			throw new ValidationException("Mindestens ein Admin-Benutzer muss erhalten bleiben");
		}
	}

	private String validateUsername(String username) {
		if (username == null || username.isBlank()) {
			throw new ValidationException("Benutzername ist erforderlich");
		}

		final var trimmedUsername = username.trim();

		if (trimmedUsername.length() < MIN_USERNAME_LENGTH
				|| trimmedUsername.length() > MAX_USERNAME_LENGTH) {
			throw new ValidationException("Benutzername muss zwischen 3 und 50 Zeichen lang sein");
		}

		if (!trimmedUsername.matches(USERNAME_PATTERN)) {
			throw new ValidationException(
					"Benutzername darf nur Buchstaben, Zahlen, '.', '_' oder '-' enthalten");
		}

		return trimmedUsername;
	}

	private void validatePassword(String password) {
		if (password == null || password.isBlank()) {
			throw new ValidationException("Passwort ist erforderlich");
		}

		if (password.length() < MIN_PASSWORD_LENGTH) {
			throw new ValidationException("Passwort muss mindestens 8 Zeichen lang sein");
		}
	}

	private WorkspaceRoleName validateWorkspaceRole(String workspaceRole) {
		if (workspaceRole == null || workspaceRole.isBlank()) {
			throw new ValidationException("Rolle ist erforderlich");
		}

		try {
			return WorkspaceRoleName.valueOf(workspaceRole.trim());
		} catch (IllegalArgumentException exception) {
			throw new ValidationException("Rolle muss ADMIN oder USER sein");
		}
	}
}
