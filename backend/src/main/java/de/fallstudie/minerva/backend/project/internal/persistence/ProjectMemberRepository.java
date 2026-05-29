package de.fallstudie.minerva.backend.project.internal.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectMemberRepository extends JpaRepository<ProjectMemberModel, Long> {
}
