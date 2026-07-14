package de.fallstudie.minerva.backend.activity.internal.persistence;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import de.fallstudie.minerva.backend.activity.ActivityScopeType;

public interface ActivityEventRepository extends JpaRepository<ActivityEventModel, Long> {
	@Query("""
			select event
			from ActivityEventModel event
			where exists (
				select ticketScope.id
				from ActivityScopeModel ticketScope
				where ticketScope.eventId = event.id
					and ticketScope.scopeType = :ticketScopeType
					and ticketScope.scopeId = :ticketId
			)
				and exists (
					select projectScope.id
					from ActivityScopeModel projectScope
					where projectScope.eventId = event.id
						and projectScope.scopeType = :projectScopeType
						and projectScope.scopeId = :projectId
				)
			order by event.occurredAt desc, event.id desc
			""")
	List<ActivityEventModel> findAllForTicketInProject(
			@Param("projectScopeType") ActivityScopeType projectScopeType,
			@Param("projectId") long projectId,
			@Param("ticketScopeType") ActivityScopeType ticketScopeType,
			@Param("ticketId") long ticketId);

	@Query("""
			select event
			from ActivityEventModel event
			where exists (
				select projectScope.id
				from ActivityScopeModel projectScope
				where projectScope.eventId = event.id
					and projectScope.scopeType = :scopeType
					and projectScope.scopeId = :projectId
			)
			order by event.occurredAt desc, event.id desc
			""")
	List<ActivityEventModel> findAllForProject(@Param("scopeType") ActivityScopeType scopeType,
			@Param("projectId") long projectId);

	/// Lädt alle aktivitäten von Projekten, bei denen ein Nutzer ein Teil ist.
	@Query("""
			select event
			from ActivityEventModel event
			where exists (
				select projectScope.id
				from ActivityScopeModel projectScope
				join ProjectMemberModel member on member.projectId = projectScope.scopeId
				where projectScope.eventId = event.id
					and projectScope.scopeType = :scopeType
					and member.userId = :userId
			)
			order by event.occurredAt desc, event.id desc
			""")
	List<ActivityEventModel> findAllForUserProjects(@Param("scopeType") ActivityScopeType scopeType,
			@Param("userId") long userId);

	/// Lädt alle Aktivitäten, welche ein Nutzer selbst durchgeführt hat.
	@Query("""
			select event
			from ActivityEventModel event
			where event.actorUserId = :userId
				and exists (
					select projectScope.id
					from ActivityScopeModel projectScope
					join ProjectMemberModel member on member.projectId = projectScope.scopeId
					where projectScope.eventId = event.id
						and projectScope.scopeType = :scopeType
						and member.userId = :userId
				)
			order by event.occurredAt desc, event.id desc
			""")
	List<ActivityEventModel> findAllForUserActor(@Param("scopeType") ActivityScopeType scopeType,
			@Param("userId") long userId);
}
