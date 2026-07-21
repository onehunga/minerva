package de.fallstudie.minerva.backend.project.internal.service;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import de.fallstudie.minerva.backend.project.ProjectEvent;
import de.fallstudie.minerva.backend.project.internal.persistence.ProjectMemberRepository;
import de.fallstudie.minerva.backend.user.UserEvent;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ProjectUserDeletionListener {
	private final ProjectMemberRepository projectMemberRepository;
	private final ApplicationEventPublisher eventPublisher;

	@EventListener
	public void on(UserEvent.Deleted event) {
		for (var member : projectMemberRepository.findAllByUserId(event.userId())) {
			projectMemberRepository.delete(member);
			eventPublisher.publishEvent(new ProjectEvent.UserRemoved(event.actorUserId(),
					member.getProjectId(), event.userId()));
		}
	}
}
