package de.fallstudie.minerva.backend.user.internal.service;

import de.fallstudie.minerva.backend.common.ResourceNotFoundException;
import de.fallstudie.minerva.backend.user.internal.persistence.UserModel;
import de.fallstudie.minerva.backend.user.internal.persistence.UserRepository;
import de.fallstudie.minerva.backend.user.internal.web.UserDetailsResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CurrentUserService {
	private final UserRepository userRepository;

	public UserDetailsResponse getUserDetails(long userId) {
		UserModel user = userRepository.findById(userId)
				.orElseThrow(() -> new ResourceNotFoundException("User not found"));
		return new UserDetailsResponse(userId, user.getUsername(),
				user.getWorkspaceRole().getName());
	}
}
