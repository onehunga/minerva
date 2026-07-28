package de.fallstudie.minerva.backend.activity.internal.service;

import de.fallstudie.minerva.backend.activity.ActivityEventType;
import de.fallstudie.minerva.backend.activity.ActivityScopeType;
import de.fallstudie.minerva.backend.activity.internal.persistence.ActivityEventModel;
import de.fallstudie.minerva.backend.activity.internal.persistence.ActivityEventRepository;
import de.fallstudie.minerva.backend.activity.internal.persistence.ActivityScopeRepository;
import de.fallstudie.minerva.backend.testsupport.TestProjects;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ActivityServiceTests {
	private ActivityEventRepository activityEventRepository;
	private ActivityService activityService;

	@BeforeEach
	void setUp() {
		activityEventRepository = Mockito.mock(ActivityEventRepository.class);
		activityService = new ActivityService(activityEventRepository,
				Mockito.mock(ActivityScopeRepository.class), new ObjectMapper());
	}

	@Test
	void deleteProjectActivitiesDeletesEveryEventWithTheProjectScope() {
		activityService.deleteProjectActivities(TestProjects.PROJECT_ID);

		verify(activityEventRepository).deleteByScope(ActivityScopeType.PROJECT,
				TestProjects.PROJECT_ID);
	}

	@Test
	void getUserActivitiesReturnsEventsFromAccessibleProjects() {
		when(activityEventRepository.findAllForUser(TestProjects.OWNER_USER_ID))
				.thenReturn(List.of(activityEvent()));

		final var response = activityService.getUserActivities(TestProjects.OWNER_USER_ID);

		assertEquals(1, response.events().size());
		assertEquals(42L, response.events().getFirst().id());
		verify(activityEventRepository).findAllForUser(TestProjects.OWNER_USER_ID);
	}

	@Test
	void getUserActorActivitiesReturnsOnlyEventsByTheCurrentUser() {
		when(activityEventRepository.findAllForUserActor(TestProjects.OWNER_USER_ID))
				.thenReturn(List.of(activityEvent()));

		final var response = activityService.getUserActorActivities(TestProjects.OWNER_USER_ID);

		assertEquals(1, response.events().size());
		assertEquals(42L, response.events().getFirst().id());
		verify(activityEventRepository).findAllForUserActor(TestProjects.OWNER_USER_ID);
	}

	private ActivityEventModel activityEvent() {
		final var event = new ActivityEventModel();
		event.setId(42L);
		event.setType(ActivityEventType.TICKET_COMMENT_CREATED);
		event.setSchemaVersion(1);
		event.setPayloadJson("{}");
		event.setOccurredAt(Instant.parse("2026-07-14T12:00:00Z"));
		return event;
	}
}
