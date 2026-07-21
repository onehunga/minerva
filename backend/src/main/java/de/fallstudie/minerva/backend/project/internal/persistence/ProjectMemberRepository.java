package de.fallstudie.minerva.backend.project.internal.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectMemberRepository extends JpaRepository<ProjectMemberModel, Long> {
	boolean existsByProjectIdAndUserId(Long projectId, Long userId);

	Optional<ProjectMemberModel> findByProjectIdAndUserId(long projectId, long userId);

	List<ProjectMemberModel> findAllByProjectId(long projectId);

	List<ProjectMemberModel> findAllByUserId(long userId);
}
