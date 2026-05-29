package de.fallstudie.minerva.backend.project.internal.persistence;

import java.time.Instant;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "projects")
@Getter
@NoArgsConstructor
public class ProjectModel {
	@Id
	@SequenceGenerator(name = "projects_id_seq", sequenceName = "projects_id_seq", allocationSize = 1)
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "projects_id_seq")
	private long id;

	@Column(nullable = false, unique = true)
	@Setter
	private String name;

	@Setter
	private String description;

	@Column(name = "created_by", nullable = false, updatable = false)
	@Setter
	private long createdBy;

	@CreationTimestamp
	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt = Instant.now();

	@UpdateTimestamp
	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt = Instant.now();
}
