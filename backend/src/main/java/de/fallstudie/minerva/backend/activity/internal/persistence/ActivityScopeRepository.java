package de.fallstudie.minerva.backend.activity.internal.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ActivityScopeRepository extends JpaRepository<ActivityScopeModel, Long> {
}
