package de.fallstudie.minerva.backend.user;

import de.fallstudie.minerva.backend.user.internal.persistence.UserModel;
import de.fallstudie.minerva.backend.user.internal.persistence.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
	private final UserRepository userRepository;

	public Optional<UserDTO> findById(long id) {
		return userRepository.findById(id).map(this::toSnapshot);
	}

	public Optional<UserDTO> findByUsername(String username) {
		return userRepository.findByUsername(username).flatMap(userModel -> {
			if (userModel.getDeactivatedAt() != null) {
				log.trace("tried finding deactivated user");
				return Optional.empty();
			}
			if (userModel.getDeletedAt() != null) {
				log.trace("tried finding deleted user");
				return Optional.empty();
			}

			return Optional.of(this.toSnapshot(userModel));
		});
	}

	public Optional<UserDTO> findActiveById(long id) {
		return userRepository.findById(id)
				.filter(user -> user.getDeletedAt() == null && user.getDeactivatedAt() == null)
				.map(this::toSnapshot);
	}

	public List<UserDTO> findAll() {
		return userRepository.findAllByDeletedAtIsNull().stream().map(this::toSnapshot).toList();
	}

	public boolean existsById(long id) {
		return this.userRepository.existsByIdAndDeletedAtIsNullAndDeactivatedAtIsNull(id);
	}

	private UserDTO toSnapshot(UserModel user) {
		return new UserDTO(user.getId(), user.getUsername(), user.getPassword(),
				user.getWorkspaceRole().getName(), user.getDeletedAt() != null);
	}
}
