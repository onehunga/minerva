package de.fallstudie.minerva.backend.user;

import de.fallstudie.minerva.backend.user.internal.persistence.WorkspaceRoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class WorkspaceRoleService {
	private final WorkspaceRoleRepository workspaceRoleRepository;

	public Optional<WorkspaceRoleModel> find(WorkspaceRoleName name) {
		return workspaceRoleRepository.findByName(name);
	}
}
