package de.fallstudie.minerva.backend.project.internal.persistence;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProjectRepository extends JpaRepository<ProjectModel, Long> {
	interface TicketCounts {
		long getProjectId();
		long getOpenTicketCount();
		long getTicketCount();
	}

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

	@Query("""
				SELECT p
				FROM ProjectModel p
				JOIN ProjectMemberModel pm ON pm.projectId = p.id
				WHERE pm.userId = :userId
				AND p.archivedAt IS NOT NULL
			""")
	List<ProjectModel> findAllArchivedByUserId(@Param("userId") long userId);

	@Query("""
			SELECT p
			FROM ProjectModel p
			WHERE p.archivedAt IS NULL
			AND NOT EXISTS (
				SELECT pm.id
				FROM ProjectMemberModel pm
				WHERE pm.projectId = p.id
				AND pm.userId = :userId
			)
			""")
	List<ProjectModel> findAllWithoutUser(@Param("userId") long userId);

	@Query("""
			SELECT p
			FROM ProjectModel p
			WHERE p.archivedAt IS NOT NULL
			AND NOT EXISTS (
				SELECT pm.id
				FROM ProjectMemberModel pm
				WHERE pm.projectId = p.id
				AND pm.userId = :userId
			)
			""")
	List<ProjectModel> findAllArchivedWithoutUser(@Param("userId") long userId);

	@Query(value = """
			SELECT t.project_id AS "projectId",
				SUM(CASE WHEN ws.workflow_status_category = 'OPEN' THEN 1 ELSE 0 END) AS "openTicketCount",
				COUNT(t.id) AS "ticketCount"
			FROM tickets t
			JOIN workflows w ON w.project_id = t.project_id AND w.ticket_type_id = t.ticket_type_id
			JOIN workflow_states ws ON ws.id = t.status_id AND ws.workflow_id = w.id
			WHERE t.project_id IN :projectIds AND t.archived_at IS NULL
			GROUP BY t.project_id
			""", nativeQuery = true)
	List<TicketCounts> countTicketsByProjectIds(@Param("projectIds") List<Long> projectIds);
}
