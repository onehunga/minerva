package de.fallstudie.minerva.backend.ticket.internal.service;

import java.util.List;
import java.util.Objects;

import de.fallstudie.minerva.backend.project.ProjectPolicies;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import de.fallstudie.minerva.backend.common.ResourceNotFoundException;
import de.fallstudie.minerva.backend.common.ValidationException;
import de.fallstudie.minerva.backend.ticket.TicketEvent;
import de.fallstudie.minerva.backend.ticket.internal.persistence.TicketModel;
import de.fallstudie.minerva.backend.ticket.internal.persistence.TicketChildRuleModel;
import de.fallstudie.minerva.backend.ticket.internal.persistence.TicketChildRuleRepository;
import de.fallstudie.minerva.backend.ticket.internal.persistence.TicketCommentRepository;
import de.fallstudie.minerva.backend.ticket.internal.persistence.TicketRepository;
import de.fallstudie.minerva.backend.ticket.internal.persistence.TicketTypeRepository;
import de.fallstudie.minerva.backend.ticket.internal.persistence.WorkflowRepository;
import de.fallstudie.minerva.backend.ticket.internal.persistence.WorkflowStatusModel;
import de.fallstudie.minerva.backend.ticket.internal.persistence.WorkflowStatusRepository;
import de.fallstudie.minerva.backend.ticket.internal.persistence.WorkflowTransitionRepository;
import de.fallstudie.minerva.backend.ticket.internal.web.CreateTicketRequest;
import de.fallstudie.minerva.backend.ticket.internal.web.TicketListResponse;
import de.fallstudie.minerva.backend.ticket.internal.web.TicketResponse;
import de.fallstudie.minerva.backend.ticket.internal.web.TicketTypeListResponse;
import de.fallstudie.minerva.backend.ticket.internal.web.TicketTypeResponse;
import de.fallstudie.minerva.backend.ticket.internal.web.UpdateTicketAssigneeRequest;
import de.fallstudie.minerva.backend.ticket.internal.web.UpdateTicketDetailsRequest;
import de.fallstudie.minerva.backend.ticket.internal.web.UpdateTicketPriorityRequest;
import de.fallstudie.minerva.backend.ticket.internal.web.UpdateTicketStatusRequest;
import de.fallstudie.minerva.backend.ticket.internal.web.WorkflowStateResponse;
import de.fallstudie.minerva.backend.ticket.internal.web.WorkflowTransitionResponse;
import de.fallstudie.minerva.backend.user.Identity;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Slf4j
@Service
@RequiredArgsConstructor
public class TicketService {
	private final ProjectPolicies projectPolicies;
	private final TicketTypeRepository ticketTypeRepository;
	private final TicketChildRuleRepository ticketChildRuleRepository;
	private final WorkflowRepository workflowRepository;
	private final WorkflowStatusRepository workflowStatusRepository;
	private final WorkflowTransitionRepository workflowTransitionRepository;
	private final TicketRepository ticketRepository;
	private final TicketCommentRepository ticketCommentRepository;
	private final ApplicationEventPublisher eventPublisher;

	public TicketListResponse getTickets(long projectId) {
		final var tickets = ticketRepository.findAllByProjectIdOrderByNameAsc(projectId).stream()
				.map(this::toTicketResponse).toList();

		return new TicketListResponse(tickets);
	}

	public TicketTypeListResponse getTicketTypes(long projectId) {
		final var ticketTypes = ticketTypeRepository.findAllByProjectIdOrderByNameAsc(projectId)
				.stream().map(ticketType -> {
					final var children = ticketChildRuleRepository
							.findAllByParentTicketIdOrderByIdAsc(ticketType.getId()).stream()
							.map(TicketChildRuleModel::getChildTicketId).toList();
					final var workflow = workflowRepository
							.findByProjectIdAndTicketTypeId(projectId, ticketType.getId())
							.orElse(null);

					if (workflow == null) {
						return new TicketTypeResponse(ticketType.getId(), ticketType.getName(),
								ticketType.getDescription(), List.of(), List.of(), children);
					}

					final var states = workflowStatusRepository
							.findAllByWorkflowIdOrderByIdAsc(workflow.getId());
					final var stateResponses = states.stream()
							.map(state -> new WorkflowStateResponse(state.getId(), state.getName(),
									state.getWorkflowStatusCategory()))
							.toList();
					final var stateIds = states.stream().map(WorkflowStatusModel::getId).toList();
					final var transitionResponses = stateIds.isEmpty()
							? List.<WorkflowTransitionResponse>of()
							: workflowTransitionRepository.findAllForWorkflowStates(stateIds)
									.stream()
									.map(transition -> new WorkflowTransitionResponse(
											transition.getId(), transition.getName(),
											transition.getFromState(), transition.getToState()))
									.toList();

					return new TicketTypeResponse(ticketType.getId(), ticketType.getName(),
							ticketType.getDescription(), stateResponses, transitionResponses,
							children);
				}).toList();

		return new TicketTypeListResponse(ticketTypes);
	}

	@Transactional
	public TicketResponse createTicket(Identity identity, long projectId,
			CreateTicketRequest request) {
		validateCreateTicketRequest(request);

		final var ticketType = ticketTypeRepository
				.findByIdAndProjectId(request.ticketTypeId(), projectId)
				.orElseThrow(() -> new ResourceNotFoundException("Ticketart nicht gefunden"));
		final var workflow = workflowRepository
				.findByProjectIdAndTicketTypeId(projectId, ticketType.getId())
				.orElseThrow(() -> new ValidationException("Ticketart hat keinen Workflow"));

		workflowStatusRepository.findByIdAndWorkflowId(request.statusId(), workflow.getId())
				.orElseThrow(() -> new ValidationException(
						"Status gehört nicht zur ausgewählten Ticketart"));

		if (request.parentTicketId() != null) {
			final var parentTicket = ticketRepository
					.findByIdAndProjectId(request.parentTicketId(), projectId)
					.orElseThrow(() -> new ValidationException(
							"Parent-Ticket gehört nicht zum Projekt"));

			ticketChildRuleRepository
					.findByParentTicketIdAndChildTicketId(parentTicket.getTicketTypeId(),
							ticketType.getId())
					.orElseThrow(() -> new ValidationException(
							"Ticketart ist als Kindticket nicht erlaubt"));
		}

		final var ticket = new TicketModel();
		ticket.setProjectId(projectId);
		ticket.setTicketTypeId(ticketType.getId());
		ticket.setStatusId(request.statusId());
		ticket.setParentTicketId(request.parentTicketId());
		ticket.setName(request.name().trim());
		ticket.setDescription(request.description() == null ? "" : request.description().trim());
		ticket.setCreatedBy(identity.userId());

		return toTicketResponse(ticketRepository.save(ticket));
	}

	@Transactional
	public void deleteTicket(long projectId, long ticketId) {
		final var ticket = ticketRepository.findByIdAndProjectId(ticketId, projectId)
				.orElseThrow(() -> new ResourceNotFoundException("Ticket nicht gefunden"));

		if (ticketRepository.existsByParentTicketId(ticket.getId())) {
			throw new ValidationException("Ticket hat noch Kindtickets");
		}

		ticketCommentRepository.deleteAllByTicketId(ticket.getId());
		ticketRepository.delete(ticket);

		log.trace("Deleted ticket with ID {} in project with ID {}", ticketId, projectId);
	}

	@Transactional
	public void updateTicketStatus(Identity identity, long projectId, long ticketId,
			UpdateTicketStatusRequest request) {
		validateUpdateTicketStatusRequest(request);

		final var ticket = ticketRepository.findByIdAndProjectId(ticketId, projectId)
				.orElseThrow(() -> new ResourceNotFoundException("Ticket nicht gefunden"));
		final var workflow = workflowRepository
				.findByProjectIdAndTicketTypeId(projectId, ticket.getTicketTypeId())
				.orElseThrow(() -> new ValidationException("Ticketart hat keinen Workflow"));
		final var transition = workflowTransitionRepository.findById(request.transitionId())
				.orElseThrow(() -> new ValidationException("Statusübergang nicht gefunden"));

		final var previousStatus = workflowStatusRepository
				.findByIdAndWorkflowId(ticket.getStatusId(), workflow.getId())
				.orElseThrow(() -> new ValidationException(
						"Aktueller Status gehört nicht zur Ticketart"));

		if (transition.getFromState() != null
				&& transition.getFromState() != ticket.getStatusId()) {
			throw new ValidationException(
					"Statusübergang ist für den aktuellen Status nicht erlaubt");
		}

		final var targetStatus = workflowStatusRepository
				.findByIdAndWorkflowId(transition.getToState(), workflow.getId()).orElseThrow(
						() -> new ValidationException("Zielstatus gehört nicht zur Ticketart"));

		ticket.setStatusId(transition.getToState());
		ticketRepository.save(ticket);
		eventPublisher.publishEvent(
				new TicketEvent.StatusChanged(identity.userId(), projectId, ticket.getId(),
						previousStatus.getId(), previousStatus.getName(), targetStatus.getId(),
						targetStatus.getName(), transition.getId(), transition.getName()));
	}

	@Transactional
	public void updateTicketDetails(long projectId, long ticketId,
			UpdateTicketDetailsRequest request) {
		validateUpdateTicketDetailsRequest(request);

		final var ticket = ticketRepository.findByIdAndProjectId(ticketId, projectId)
				.orElseThrow(() -> new ResourceNotFoundException("Ticket nicht gefunden"));
		final var newName = request.name().trim();
		final var newDescription = request.description() == null
				? ""
				: request.description().trim();

		ticket.setName(newName);
		ticket.setDescription(newDescription);
		ticketRepository.save(ticket);
	}

	@Transactional
	public void updateTicketPriority(long projectId, long ticketId,
			UpdateTicketPriorityRequest request) {
		validateUpdateTicketPriorityRequest(request);

		final var ticket = ticketRepository.findByIdAndProjectId(ticketId, projectId)
				.orElseThrow(() -> new ResourceNotFoundException("Ticket nicht gefunden"));

		ticket.setPriority(request.priority());
		ticketRepository.save(ticket);
	}

	@Transactional
	public void updateTicketAssignee(Identity identity, long projectId, long ticketId,
			UpdateTicketAssigneeRequest request) {
		validateUpdateTicketAssigneeRequest(request);

		final var ticket = ticketRepository.findByIdAndProjectId(ticketId, projectId)
				.orElseThrow(() -> new ResourceNotFoundException("Ticket nicht gefunden"));
		final var previousAssigneeId = ticket.getAssignedTo();

		if (request.assignedTo() != null) {
			if (!this.projectPolicies.canBeAssigned(projectId, request.assignedTo())) {
				throw new ValidationException(
						"Der Nutzer kann diesem Projekt nicht zugewiesen werden");
			}
		}

		ticket.setAssignedTo(request.assignedTo());
		ticketRepository.save(ticket);
		if (!Objects.equals(previousAssigneeId, request.assignedTo())) {
			eventPublisher.publishEvent(new TicketEvent.AssigneeChanged(identity.userId(),
					projectId, ticket.getId(), previousAssigneeId, request.assignedTo()));
		}
	}

	private TicketResponse toTicketResponse(TicketModel ticket) {
		return new TicketResponse(ticket.getId(), ticket.getProjectId(), ticket.getTicketTypeId(),
				ticket.getStatusId(), ticket.getPriority(), ticket.getParentTicketId(),
				ticket.getName(), ticket.getDescription(), ticket.getCreatedBy(),
				ticket.getAssignedTo(), ticket.getCreatedAt(), ticket.getUpdatedAt());
	}

	private void validateCreateTicketRequest(CreateTicketRequest request) {
		if (request == null) {
			throw new IllegalArgumentException("Request must not be null");
		}

		if (request.name() == null || request.name().isBlank()) {
			throw new ValidationException("Ticket muss einen Namen haben");
		}

		if (request.name().length() > 255) {
			throw new ValidationException("Ticketname darf maximal 255 Zeichen lang sein");
		}

		if (request.description() != null && request.description().length() > 255) {
			throw new ValidationException("Ticketbeschreibung darf maximal 255 Zeichen lang sein");
		}

		if (request.ticketTypeId() <= 0) {
			throw new ValidationException("Ticketart ist erforderlich");
		}

		if (request.statusId() <= 0) {
			throw new ValidationException("Status ist erforderlich");
		}

		if (request.parentTicketId() != null && request.parentTicketId() <= 0) {
			throw new ValidationException("Parent-Ticket ist ungültig");
		}
	}

	private void validateUpdateTicketStatusRequest(UpdateTicketStatusRequest request) {
		if (request == null) {
			throw new IllegalArgumentException("Request must not be null");
		}

		if (request.transitionId() <= 0) {
			throw new ValidationException("Statusübergang ist erforderlich");
		}
	}

	private void validateUpdateTicketPriorityRequest(UpdateTicketPriorityRequest request) {
		if (request == null) {
			throw new IllegalArgumentException("Request must not be null");
		}

		if (request.priority() == null) {
			throw new ValidationException("Priorität ist erforderlich");
		}
	}

	private void validateUpdateTicketDetailsRequest(UpdateTicketDetailsRequest request) {
		if (request == null) {
			throw new IllegalArgumentException("Request must not be null");
		}

		if (request.name() == null || request.name().isBlank()) {
			throw new ValidationException("Ticket muss einen Namen haben");
		}

		if (request.name().length() > 255) {
			throw new ValidationException("Ticketname darf maximal 255 Zeichen lang sein");
		}

		if (request.description() != null && request.description().length() > 255) {
			throw new ValidationException("Ticketbeschreibung darf maximal 255 Zeichen lang sein");
		}
	}

	private void validateUpdateTicketAssigneeRequest(UpdateTicketAssigneeRequest request) {
		if (request == null) {
			throw new IllegalArgumentException("Request must not be null");
		}
	}
}
