package de.fallstudie.minerva.backend.project.internal.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest(properties = {"spring.jpa.hibernate.ddl-auto=create-drop",
		"spring.liquibase.enabled=false"})
@ActiveProfiles("test")
class ProjectRepositoryTests {
	@Autowired
	private ProjectRepository projectRepository;

	@Autowired
	private ProjectMemberRepository projectMemberRepository;

	@Test
	void findAllByUserIdReturnsOnlyProjectsForUser() {
		final var firstUserProject = createProject("First user project", 1L);
		final var sharedProject = createProject("Shared project", 2L);
		final var otherUserProject = createProject("Other user project", 2L);
		final var archivedProject = createProject("Archived project", 1L);
		archivedProject.setArchivedAt(Instant.now());
		projectRepository.saveAndFlush(archivedProject);

		createMembership(firstUserProject, 42L);
		createMembership(sharedProject, 42L);
		createMembership(sharedProject, 84L);
		createMembership(otherUserProject, 84L);
		createMembership(archivedProject, 42L);

		final var projects = projectRepository.findAllByUserId(42L);

		assertEquals(List.of(firstUserProject, sharedProject), projects);
	}

	@Test
	void findAllWithoutUserReturnsActiveNonMemberAndEmptyProjects() {
		final var memberProject = createProject("Member project", 1L);
		final var otherProject = createProject("Other project", 2L);
		final var emptyProject = createProject("Empty project", 2L);
		final var archivedProject = createProject("Archived project", 2L);
		archivedProject.setArchivedAt(Instant.now());
		projectRepository.saveAndFlush(archivedProject);
		createMembership(memberProject, 42L);
		createMembership(otherProject, 84L);

		final var projects = projectRepository.findAllWithoutUser(42L);

		assertEquals(List.of(otherProject, emptyProject), projects);
	}

	private ProjectModel createProject(String name, long createdBy) {
		final var project = new ProjectModel();
		project.setName(name);
		project.setDescription(name + " description");
		project.setCreatedBy(createdBy);

		return projectRepository.saveAndFlush(project);
	}

	private void createMembership(ProjectModel project, long userId) {
		final var member = new ProjectMemberModel();
		member.setProjectId(project.getId());
		member.setUserId(userId);
		member.setRoleId(1L);

		projectMemberRepository.saveAndFlush(member);
	}
}
