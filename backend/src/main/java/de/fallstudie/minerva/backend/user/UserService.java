package de.fallstudie.minerva.backend.user;

import de.fallstudie.minerva.backend.user.internal.persistence.UserModel;
import de.fallstudie.minerva.backend.user.internal.persistence.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {
	private final UserRepository userRepository;

	public Optional<UserDTO> findById(long id) {
		return userRepository.findById(id).map(this::toSnapshot);
	}

	public Optional<UserDTO> findByUsername(String username) {
		return userRepository.findByUsername(username).map(this::toSnapshot);
	}

	public List<UserDTO> findAll() {
		return userRepository.findAll().stream().map(this::toSnapshot).toList();
	}

	public boolean existsById(long id) {
		return userRepository.existsById(id);
	}

	private UserDTO toSnapshot(UserModel user) {
		return new UserDTO(user.getId(), user.getUsername(), user.getPassword(),
				user.getWorkspaceRole().getName());
	}
}
