package de.fallstudie.minerva.backend.project;

import java.util.List;

public interface IProjectStatisticsService {
	List<ProjectStatisticsProject> getProjectsForUser(long userId);
}
