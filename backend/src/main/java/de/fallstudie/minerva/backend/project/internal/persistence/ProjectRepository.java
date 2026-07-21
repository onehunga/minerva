package de.fallstudie.minerva.backend.project.internal.persistence;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProjectRepository extends JpaRepository<ProjectModel, Long> {
	boolean existsByIdAndArchivedAtIsNotNull(long id);

	List<ProjectModel> findAllByArchivedAtIsNull();

	@Query("""
				SELECT p
				FROM ProjectModel p
				JOIN ProjectMemberModel pm ON pm.projectId = p.id
				WHERE pm.userId = :userId
				AND p.archivedAt IS NULL
			""")
	List<ProjectModel> findAllByUserId(@Param("userId") long userId);
}
