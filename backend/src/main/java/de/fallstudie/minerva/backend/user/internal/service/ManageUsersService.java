package de.fallstudie.minerva.backend.user.internal.service;

import de.fallstudie.minerva.backend.authorization.WorkspaceRoleName;
import de.fallstudie.minerva.backend.authorization.WorkspaceRoleService;
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
	private final PasswordEncoder passwordEncoder;
	private final WorkspaceRoleService workspaceRoleService;
	private final UserRepository userRepository;

	@Transactional
	public void createUser(String username, String password, String workspaceRole) {
		final var role = workspaceRoleService.find(WorkspaceRoleName.valueOf(workspaceRole))
				.orElseThrow();

		final var user = new UserModel();
		user.setUsername(username);
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
}
