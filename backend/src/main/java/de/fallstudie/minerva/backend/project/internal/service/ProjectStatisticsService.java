package de.fallstudie.minerva.backend.project.internal.service;

import java.util.List;

import org.springframework.stereotype.Service;

import de.fallstudie.minerva.backend.project.IProjectStatisticsService;
import de.fallstudie.minerva.backend.project.ProjectStatisticsProject;
import de.fallstudie.minerva.backend.project.internal.persistence.ProjectRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProjectStatisticsService implements IProjectStatisticsService {
	private final ProjectRepository projectRepository;

	@Override
	public List<ProjectStatisticsProject> getProjectsForUser(long userId) {
		return projectRepository.findAllByUserId(userId).stream()
				.map(project -> new ProjectStatisticsProject(project.getId(), project.getName()))
				.toList();
	}
}
