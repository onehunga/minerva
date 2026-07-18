package de.fallstudie.minerva.backend.ticket;

import de.fallstudie.minerva.backend.common.DuplicateResourceException;
import de.fallstudie.minerva.backend.ticket.internal.WorkflowConfigurationsValidator;
import de.fallstudie.minerva.backend.ticket.internal.persistence.*;
import jakarta.transaction.Transactional;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class TicketConfigurationService {
	private final TicketTypeRepository ticketTypeRepository;
	private final WorkflowRepository workflowRepository;
	private final WorkflowStatusRepository workflowStatusRepository;
	private final WorkflowTransitionRepository workflowTransitionRepository;
	private final TicketChildRuleRepository ticketChildRuleRepository;

	@Transactional
	public void createTicketConfiguration(long projectId, WorkflowConfigurationsCreateRequest req) {
		new WorkflowConfigurationsValidator(req).validate();

		if (ticketTypeRepository.existsByProjectId(projectId)) {
			throw new DuplicateResourceException(
					"Ticketkonfiguration existiert bereits für dieses Projekt");
		}

		log.info("Creating ticket configuration for project {}", projectId);

		final var ticketTypes = new HashMap<String, Long>();

		for (final var ticket : req.tickets()) {
			final var ticketId = this.generateTicketType(projectId, ticket);

			ticketTypes.put(ticket.name(), ticketId);
		}

		for (final var ticket : req.tickets()) {
			this.generateTicketChildren(ticket, ticketTypes);
		}
	}

	@Transactional
	protected long generateTicketType(long projectId,
			@NonNull TicketTypeConfigurationRequest ticket) {
		final var ticketTypeModel = new TicketTypeModel();
		ticketTypeModel.setProjectId(projectId);
		ticketTypeModel.setName(ticket.name());
		ticketTypeModel.setDescription(ticket.description());

		this.ticketTypeRepository.save(ticketTypeModel);

		this.generateTicketWorkflow(projectId, ticketTypeModel.getId(), ticket);

		return ticketTypeModel.getId();
	}

	@Transactional
	protected void generateTicketWorkflow(long projectId, long ticketTypeId,
			@NonNull TicketTypeConfigurationRequest ticket) {
		final var workflowModel = new WorkflowModel();
		workflowModel.setTicketTypeId(ticketTypeId);
		workflowModel.setProjectId(projectId);

		this.workflowRepository.save(workflowModel);
		final var map = new HashMap<String, Long>();

		for (final var ticketState : ticket.states()) {
			final var ticketStateModel = new WorkflowStatusModel();
			ticketStateModel.setName(ticketState.name());
			ticketStateModel.setWorkflowId(workflowModel.getId());
			ticketStateModel.setWorkflowStatusCategory(
					TicketStatusCategory.valueOf(ticketState.category()));

			this.workflowStatusRepository.save(ticketStateModel);

			map.put(ticketState.name(), ticketStateModel.getId());
		}

		for (final var ticketTransition : ticket.transitions()) {
			final var ticketTransitionModel = new WorkflowTransitionModel();
			ticketTransitionModel.setName(ticketTransition.name());
			ticketTransitionModel.setFromState(
					ticketTransition.from() == null ? null : map.get(ticketTransition.from()));
			ticketTransitionModel.setToState(map.get(ticketTransition.to()));

			this.workflowTransitionRepository.save(ticketTransitionModel);
		}
	}

	protected void generateTicketChildren(TicketTypeConfigurationRequest ticket,
			Map<String, Long> ticketTypes) {
		for (final var child : ticket.children()) {
			final var ticketChildRuleModel = new TicketChildRuleModel();
			ticketChildRuleModel.setParentTicketId(ticketTypes.get(ticket.name()));
			ticketChildRuleModel.setChildTicketId(ticketTypes.get(child));

			this.ticketChildRuleRepository.save(ticketChildRuleModel);
		}
	}
}
