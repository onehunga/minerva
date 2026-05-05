package de.fallstudie.minerva.backend.user;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {
	private final UserRepository userRepository;

	public UserDetailsResponse getUserDetails(long userId) {
		UserModel user = userRepository.findById(userId)
				.orElseThrow(() -> new RuntimeException("User not found"));
		return new UserDetailsResponse(user.getUsername());
	}
}
