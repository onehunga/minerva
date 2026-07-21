package de.fallstudie.minerva.backend.testsupport;

import java.util.List;

import org.springframework.test.util.ReflectionTestUtils;

import de.fallstudie.minerva.backend.project.CreateProjectCommand;
import de.fallstudie.minerva.backend.project.internal.persistence.ProjectMemberModel;
import de.fallstudie.minerva.backend.project.internal.persistence.ProjectModel;
import de.fallstudie.minerva.backend.project.internal.persistence.ProjectRoleModel;
import de.fallstudie.minerva.backend.project.internal.persistence.ProjectRoleName;
import de.fallstudie.minerva.backend.projectsetup.internal.web.ProjectSetupRequest;
import de.fallstudie.minerva.backend.ticket.TicketStateConfigurationRequest;
import de.fallstudie.minerva.backend.ticket.TicketStatusCategory;
import de.fallstudie.minerva.backend.ticket.TicketStateTransitionConfigurationRequest;
import de.fallstudie.minerva.backend.ticket.TicketTypeConfigurationRequest;
import de.fallstudie.minerva.backend.ticket.WorkflowConfigurationsCreateRequest;
import de.fallstudie.minerva.backend.ticket.internal.persistence.TicketChildRuleModel;
import de.fallstudie.minerva.backend.ticket.internal.persistence.TicketModel;
import de.fallstudie.minerva.backend.ticket.internal.persistence.TicketTypeModel;
import de.fallstudie.minerva.backend.ticket.internal.persistence.WorkflowModel;
import de.fallstudie.minerva.backend.ticket.internal.persistence.WorkflowStatusModel;
import de.fallstudie.minerva.backend.ticket.internal.persistence.WorkflowTransitionModel;
import de.fallstudie.minerva.backend.user.Identity;
import de.fallstudie.minerva.backend.user.UserDTO;
import de.fallstudie.minerva.backend.user.WorkspaceRoleName;

public final class TestProjects {
	public static final long PROJECT_ID = 7L;
	public static final long OTHER_PROJECT_ID = 8L;
	public static final long OWNER_USER_ID = 42L;
	public static final long CONTRIBUTOR_USER_ID = 43L;
	public static final long VIEWER_USER_ID = 44L;
	public static final long OUTSIDER_USER_ID = 99L;
	public static final Identity OWNER = new Identity(OWNER_USER_ID);

	public static final long OWNER_ROLE_ID = 100L;
	public static final long CONTRIBUTOR_ROLE_ID = 101L;
	public static final long VIEWER_ROLE_ID = 102L;

	public static final long PARENT_TICKET_TYPE_ID = 1L;
	public static final long CHILD_TICKET_TYPE_ID = 2L;
	public static final long FORBIDDEN_CHILD_TICKET_TYPE_ID = 3L;
	public static final long WORKFLOW_ID = 10L;
	public static final long OPEN_STATUS_ID = 20L;
	public static final long IN_PROGRESS_STATUS_ID = 21L;
	public static final long DONE_STATUS_ID = 22L;
	public static final long START_PROGRESS_TRANSITION_ID = 30L;
	public static final long ANY_DONE_TRANSITION_ID = 31L;
	public static final long PARENT_TICKET_ID = 40L;
	public static final long CHILD_TICKET_ID = 41L;

	private TestProjects() {
	}

	public static CreateProjectCommand createProjectCommand() {
		return new CreateProjectCommand("Minerva", "Ticket project");
	}

	public static CreateProjectCommand createProjectCommand(String name, String description) {
		return new CreateProjectCommand(name, description);
	}

	public static ProjectSetupRequest projectSetupRequest() {
		return new ProjectSetupRequest("Minerva", "Ticket project", workflowConfiguration());
	}

	public static WorkflowConfigurationsCreateRequest workflowConfiguration() {
		return new WorkflowConfigurationsCreateRequest(List.of(parentTicketConfiguration(),
				childTicketConfiguration(), forbiddenChildTicketConfiguration()));
	}

	public static TicketTypeConfigurationRequest parentTicketConfiguration() {
		return new TicketTypeConfigurationRequest("Epic", "Large work items", workflowStates(),
				workflowTransitions(), List.of("Task"));
	}

	public static TicketTypeConfigurationRequest childTicketConfiguration() {
		return new TicketTypeConfigurationRequest("Task", "Child work items", workflowStates(),
				workflowTransitions(), List.of());
	}

	public static TicketTypeConfigurationRequest forbiddenChildTicketConfiguration() {
		return new TicketTypeConfigurationRequest("Bug", "Forbidden child type", workflowStates(),
				workflowTransitions(), List.of());
	}

	public static List<TicketStateConfigurationRequest> workflowStates() {
		return List.of(new TicketStateConfigurationRequest("Open", "OPEN"),
				new TicketStateConfigurationRequest("In Progress", "IN_PROGRESS"),
				new TicketStateConfigurationRequest("Done", "COMPLETED"));
	}

	public static List<TicketStateTransitionConfigurationRequest> workflowTransitions() {
		return List.of(
				new TicketStateTransitionConfigurationRequest("Start", "Open", "In Progress"),
				new TicketStateTransitionConfigurationRequest("Close from any", null, "Done"));
	}

	public static ProjectModel project(long id) {
		final var project = new ProjectModel();
		ReflectionTestUtils.setField(project, "id", id);
		project.setName("Minerva");
		project.setDescription("Ticket project");
		project.setCreatedBy(OWNER_USER_ID);
		return project;
	}

	public static ProjectRoleModel projectRole(long id, long projectId, ProjectRoleName name) {
		final var role = new ProjectRoleModel();
		ReflectionTestUtils.setField(role, "id", id);
		role.setProjectId(projectId);
		role.setName(name);
		return role;
	}

	public static ProjectMemberModel projectMember(long projectId, long userId, long roleId) {
		final var member = new ProjectMemberModel();
		member.setProjectId(projectId);
		member.setUserId(userId);
		member.setRoleId(roleId);
		return member;
	}

	public static UserDTO user(long id, String username) {
		return new UserDTO(id, username, "hash", WorkspaceRoleName.USER, false);
	}

	public static TicketTypeModel ticketType(long id, String name) {
		final var ticketType = new TicketTypeModel();
		ReflectionTestUtils.setField(ticketType, "id", id);
		ticketType.setProjectId(PROJECT_ID);
		ticketType.setName(name);
		ticketType.setDescription(name + " description");
		return ticketType;
	}

	public static WorkflowModel workflow(long id, long projectId, long ticketTypeId) {
		final var workflow = new WorkflowModel();
		ReflectionTestUtils.setField(workflow, "id", id);
		workflow.setProjectId(projectId);
		workflow.setTicketTypeId(ticketTypeId);
		return workflow;
	}

	public static WorkflowStatusModel workflowStatus(long id, long workflowId, String name,
			TicketStatusCategory category) {
		final var status = new WorkflowStatusModel();
		ReflectionTestUtils.setField(status, "id", id);
		status.setWorkflowId(workflowId);
		status.setName(name);
		status.setWorkflowStatusCategory(category);
		return status;
	}

	public static WorkflowStatusModel openStatus(long workflowId) {
		return workflowStatus(OPEN_STATUS_ID, workflowId, "Open", TicketStatusCategory.OPEN);
	}

	public static WorkflowStatusModel inProgressStatus(long workflowId) {
		return workflowStatus(IN_PROGRESS_STATUS_ID, workflowId, "In Progress",
				TicketStatusCategory.IN_PROGRESS);
	}

	public static WorkflowStatusModel doneStatus(long workflowId) {
		return workflowStatus(DONE_STATUS_ID, workflowId, "Done", TicketStatusCategory.COMPLETED);
	}

	public static WorkflowTransitionModel workflowTransition(long id, Long fromStateId,
			long toStateId) {
		final var transition = new WorkflowTransitionModel();
		ReflectionTestUtils.setField(transition, "id", id);
		transition.setName("Transition " + id);
		transition.setFromState(fromStateId);
		transition.setToState(toStateId);
		return transition;
	}

	public static WorkflowTransitionModel startProgressTransition() {
		return workflowTransition(START_PROGRESS_TRANSITION_ID, OPEN_STATUS_ID,
				IN_PROGRESS_STATUS_ID);
	}

	public static WorkflowTransitionModel anyDoneTransition() {
		return workflowTransition(ANY_DONE_TRANSITION_ID, null, DONE_STATUS_ID);
	}

	public static TicketModel ticket(long id, long projectId, long ticketTypeId, long statusId) {
		final var ticket = new TicketModel();
		ReflectionTestUtils.setField(ticket, "id", id);
		ticket.setProjectId(projectId);
		ticket.setTicketTypeId(ticketTypeId);
		ticket.setStatusId(statusId);
		ticket.setName("Ticket " + id);
		ticket.setDescription("Description " + id);
		ticket.setCreatedBy(OWNER_USER_ID);
		return ticket;
	}

	public static TicketChildRuleModel childRule(long parentTicketTypeId, long childTicketTypeId) {
		final var childRule = new TicketChildRuleModel();
		childRule.setParentTicketId(parentTicketTypeId);
		childRule.setChildTicketId(childTicketTypeId);
		return childRule;
	}
}
