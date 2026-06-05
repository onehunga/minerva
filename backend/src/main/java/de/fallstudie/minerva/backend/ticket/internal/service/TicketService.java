package de.fallstudie.minerva.backend.ticket.internal.service;

import java.util.List;

import org.springframework.stereotype.Service;

import de.fallstudie.minerva.backend.common.ResourceNotFoundException;
import de.fallstudie.minerva.backend.common.ValidationException;
import de.fallstudie.minerva.backend.ticket.internal.persistence.TicketModel;
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
import de.fallstudie.minerva.backend.ticket.internal.web.UpdateTicketStatusRequest;
import de.fallstudie.minerva.backend.ticket.internal.web.WorkflowStateResponse;
import de.fallstudie.minerva.backend.ticket.internal.web.WorkflowTransitionResponse;
import de.fallstudie.minerva.backend.user.Identity;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TicketService {
	private final TicketTypeRepository ticketTypeRepository;
	private final WorkflowRepository workflowRepository;
	private final WorkflowStatusRepository workflowStatusRepository;
	private final WorkflowTransitionRepository workflowTransitionRepository;
	private final TicketRepository ticketRepository;

	public TicketListResponse getTickets(long projectId) {
		final var tickets = ticketRepository.findAllByProjectIdOrderByNameAsc(projectId).stream()
				.map(this::toTicketResponse).toList();

		return new TicketListResponse(tickets);
	}

	public TicketTypeListResponse getTicketTypes(long projectId) {
		final var ticketTypes = ticketTypeRepository.findAllByProjectIdOrderByNameAsc(projectId)
				.stream().map(ticketType -> {
					final var workflow = workflowRepository
							.findByProjectIdAndTicketTypeId(projectId, ticketType.getId())
							.orElse(null);

					if (workflow == null) {
						return new TicketTypeResponse(ticketType.getId(), ticketType.getName(),
								ticketType.getDescription(), List.of(), List.of());
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
							: workflowTransitionRepository
									.findAllByFromStateInOrderByIdAsc(stateIds).stream()
									.map(transition -> new WorkflowTransitionResponse(
											transition.getId(), transition.getName(),
											transition.getFromState(), transition.getToState()))
									.toList();

					return new TicketTypeResponse(ticketType.getId(), ticketType.getName(),
							ticketType.getDescription(), stateResponses, transitionResponses);
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
						"Status gehoert nicht zur ausgewaehlten Ticketart"));

		final var ticket = new TicketModel();
		ticket.setProjectId(projectId);
		ticket.setTicketTypeId(ticketType.getId());
		ticket.setStatusId(request.statusId());
		ticket.setName(request.name().trim());
		ticket.setDescription(request.description() == null ? "" : request.description().trim());
		ticket.setCreatedBy(identity.userId());

		final var savedTicket = ticketRepository.save(ticket);

		return toTicketResponse(savedTicket);
	}

	@Transactional
	public void updateTicketStatus(long projectId, long ticketId,
			UpdateTicketStatusRequest request) {
		validateUpdateTicketStatusRequest(request);

		final var ticket = ticketRepository.findByIdAndProjectId(ticketId, projectId)
				.orElseThrow(() -> new ResourceNotFoundException("Ticket nicht gefunden"));
		final var workflow = workflowRepository
				.findByProjectIdAndTicketTypeId(projectId, ticket.getTicketTypeId())
				.orElseThrow(() -> new ValidationException("Ticketart hat keinen Workflow"));
		final var transition = workflowTransitionRepository.findById(request.transitionId())
				.orElseThrow(() -> new ValidationException("Statusuebergang nicht gefunden"));

		workflowStatusRepository.findByIdAndWorkflowId(transition.getToState(), workflow.getId())
				.orElseThrow(
						() -> new ValidationException("Zielstatus gehoert nicht zur Ticketart"));

		if (transition.getFromState() != ticket.getStatusId()) {
			throw new ValidationException(
					"Statusuebergang ist fuer den aktuellen Status nicht erlaubt");
		}

		ticket.setStatusId(transition.getToState());
		ticketRepository.save(ticket);
	}

	private TicketResponse toTicketResponse(TicketModel ticket) {
		return new TicketResponse(ticket.getId(), ticket.getProjectId(), ticket.getTicketTypeId(),
				ticket.getStatusId(), ticket.getName(), ticket.getDescription(),
				ticket.getCreatedBy(), ticket.getAssignedTo(), ticket.getCreatedAt(),
				ticket.getUpdatedAt());
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
	}

	private void validateUpdateTicketStatusRequest(UpdateTicketStatusRequest request) {
		if (request == null) {
			throw new IllegalArgumentException("Request must not be null");
		}

		if (request.transitionId() <= 0) {
			throw new ValidationException("Statusuebergang ist erforderlich");
		}
	}
}
