package de.fallstudie.minerva.backend.project.internal.service;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;

import de.fallstudie.minerva.backend.project.ProjectEvent;
import de.fallstudie.minerva.backend.project.internal.persistence.ProjectMemberRepository;
import de.fallstudie.minerva.backend.testsupport.TestProjects;
import de.fallstudie.minerva.backend.user.UserEvent;

class ProjectUserDeletionListenerTests {
	@Test
	void removesEveryProjectMembershipAndPublishesRemovalEvents() {
		final var repository = mock(ProjectMemberRepository.class);
		final var eventPublisher = mock(ApplicationEventPublisher.class);
		final var listener = new ProjectUserDeletionListener(repository, eventPublisher);
		final var member = TestProjects.projectMember(TestProjects.PROJECT_ID,
				TestProjects.OUTSIDER_USER_ID, TestProjects.VIEWER_ROLE_ID);
		when(repository.findAllByUserId(TestProjects.OUTSIDER_USER_ID)).thenReturn(List.of(member));

		listener.on(
				new UserEvent.Deleted(TestProjects.OWNER_USER_ID, TestProjects.OUTSIDER_USER_ID));

		verify(repository).delete(member);
		verify(eventPublisher).publishEvent(new ProjectEvent.UserRemoved(TestProjects.OWNER_USER_ID,
				TestProjects.PROJECT_ID, TestProjects.OUTSIDER_USER_ID));
	}
}
