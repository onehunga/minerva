package de.fallstudie.minerva.backend.user.internal.bootstrap;

import de.fallstudie.minerva.backend.user.WorkspaceRoleName;
import de.fallstudie.minerva.backend.user.WorkspaceRoleService;
import de.fallstudie.minerva.backend.user.internal.persistence.UserModel;
import de.fallstudie.minerva.backend.user.internal.persistence.UserRepository;
import de.fallstudie.minerva.backend.user.internal.persistence.WorkspaceRoleModel;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class InitialAdminBootstrap implements ApplicationRunner {
	private static final String ADMIN_USERNAME = "admin";

	private final UserRepository userRepository;
	private final WorkspaceRoleService workspaceRoleService;
	private final PasswordEncoder passwordEncoder;

	@Value("${INITIAL_ADMIN_PASSWORD:}")
	private String initialAdminPassword;

	@Override
	@Transactional
	public void run(@NonNull ApplicationArguments args) {
		if (initialAdminPassword == null || initialAdminPassword.isBlank()) {
			return;
		}

		if (userRepository.existsByUsernameAndDeletedAtIsNull(ADMIN_USERNAME)) {
			return;
		}

		WorkspaceRoleModel adminRole = workspaceRoleService.find(WorkspaceRoleName.ADMIN)
				.orElseThrow(() -> new IllegalStateException("ADMIN workspace role is missing"));

		UserModel adminUser = new UserModel();
		adminUser.setUsername(ADMIN_USERNAME);
		adminUser.setPassword(passwordEncoder.encode(initialAdminPassword));
		adminUser.setWorkspaceRole(adminRole);

		userRepository.save(adminUser);
	}
}
