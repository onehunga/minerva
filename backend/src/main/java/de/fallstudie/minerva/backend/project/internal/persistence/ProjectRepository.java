package de.fallstudie.minerva.backend.project.internal.persistence;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProjectRepository extends JpaRepository<ProjectModel, Long> {
	boolean existsByName(String name);

	@Query("""
			SELECT p
			FROM ProjectModel p
			JOIN ProjectMemberModel pm ON pm.projectId = p.id
			WHERE pm.userId = :userId
			""")
	List<ProjectModel> findAllByUserId(@Param("userId") long userId);
}
