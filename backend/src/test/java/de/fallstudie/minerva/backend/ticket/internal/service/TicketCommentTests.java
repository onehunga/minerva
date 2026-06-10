package de.fallstudie.minerva.backend.ticket.internal.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

import de.fallstudie.minerva.backend.common.ResourceNotFoundException;
import de.fallstudie.minerva.backend.common.ValidationException;
import de.fallstudie.minerva.backend.testsupport.TestProjects;
import de.fallstudie.minerva.backend.ticket.internal.persistence.TicketCommentModel;
import de.fallstudie.minerva.backend.ticket.internal.persistence.TicketCommentRepository;
import de.fallstudie.minerva.backend.ticket.internal.persistence.TicketRepository;
import de.fallstudie.minerva.backend.ticket.internal.web.CreateTicketCommentRequest;
import de.fallstudie.minerva.backend.user.UserService;

class TicketCommentTests {
	private TicketRepository ticketRepository;
	private TicketCommentRepository ticketCommentRepository;
	private UserService userService;
	private TicketCommentService ticketCommentService;

	@BeforeEach
	void setUp() {
		ticketRepository = Mockito.mock(TicketRepository.class);
		ticketCommentRepository = Mockito.mock(TicketCommentRepository.class);
		userService = Mockito.mock(UserService.class);
		ticketCommentService = new TicketCommentService(ticketRepository, ticketCommentRepository,
				userService);
	}

	@Test
	void getTicketCommentsReturnsCommentsForProjectTicket() {
		final var ticket = TestProjects.ticket(TestProjects.CHILD_TICKET_ID,
				TestProjects.PROJECT_ID, TestProjects.CHILD_TICKET_TYPE_ID,
				TestProjects.OPEN_STATUS_ID);
		final var comment = ticketComment(1L, ticket.getId(), TestProjects.OWNER_USER_ID,
				"Erster Kommentar");
		when(ticketRepository.findByIdAndProjectId(ticket.getId(), TestProjects.PROJECT_ID))
				.thenReturn(Optional.of(ticket));
		when(ticketCommentRepository.findAllByTicketIdOrderByCreatedAtAscIdAsc(ticket.getId()))
				.thenReturn(List.of(comment));
		when(userService.findById(TestProjects.OWNER_USER_ID))
				.thenReturn(Optional.of(TestProjects.user(TestProjects.OWNER_USER_ID, "owner")));

		final var response = ticketCommentService.getTicketComments(TestProjects.PROJECT_ID,
				ticket.getId());

		assertEquals(1, response.comments().size());
		assertEquals(comment.getId(), response.comments().getFirst().id());
		assertEquals(ticket.getId(), response.comments().getFirst().ticketId());
		assertEquals(TestProjects.OWNER_USER_ID, response.comments().getFirst().authorId());
		assertEquals("owner", response.comments().getFirst().authorUsername());
		assertEquals("Erster Kommentar", response.comments().getFirst().content());
	}

	@Test
	void getTicketCommentsRejectsUnknownTicket() {
		when(ticketRepository.findByIdAndProjectId(TestProjects.CHILD_TICKET_ID,
				TestProjects.PROJECT_ID)).thenReturn(Optional.empty());

		assertThrows(ResourceNotFoundException.class, () -> ticketCommentService
				.getTicketComments(TestProjects.PROJECT_ID, TestProjects.CHILD_TICKET_ID));

		verify(ticketCommentRepository, never())
				.findAllByTicketIdOrderByCreatedAtAscIdAsc(anyLong());
	}

	@Test
	void createTicketCommentSavesTrimmedContentAndAuthor() {
		final var ticket = TestProjects.ticket(TestProjects.CHILD_TICKET_ID,
				TestProjects.PROJECT_ID, TestProjects.CHILD_TICKET_TYPE_ID,
				TestProjects.OPEN_STATUS_ID);
		when(ticketRepository.findByIdAndProjectId(ticket.getId(), TestProjects.PROJECT_ID))
				.thenReturn(Optional.of(ticket));
		when(ticketCommentRepository.save(any(TicketCommentModel.class)))
				.thenAnswer(invocation -> invocation.getArgument(0));
		when(userService.findById(TestProjects.OWNER_USER_ID))
				.thenReturn(Optional.of(TestProjects.user(TestProjects.OWNER_USER_ID, "owner")));

		final var response = ticketCommentService.createTicketComment(TestProjects.OWNER,
				TestProjects.PROJECT_ID, ticket.getId(),
				new CreateTicketCommentRequest("  Sieht gut aus.  "));

		assertEquals(ticket.getId(), response.ticketId());
		assertEquals(TestProjects.OWNER_USER_ID, response.authorId());
		assertEquals("owner", response.authorUsername());
		assertEquals("Sieht gut aus.", response.content());
		final var commentCaptor = ArgumentCaptor.forClass(TicketCommentModel.class);
		verify(ticketCommentRepository).save(commentCaptor.capture());
		assertEquals(ticket.getId(), commentCaptor.getValue().getTicketId());
		assertEquals(TestProjects.OWNER_USER_ID, commentCaptor.getValue().getAuthorId());
		assertEquals("Sieht gut aus.", commentCaptor.getValue().getContent());
	}

	@Test
	void createTicketCommentRejectsUnknownTicket() {
		when(ticketRepository.findByIdAndProjectId(TestProjects.CHILD_TICKET_ID,
				TestProjects.PROJECT_ID)).thenReturn(Optional.empty());

		assertThrows(ResourceNotFoundException.class,
				() -> ticketCommentService.createTicketComment(TestProjects.OWNER,
						TestProjects.PROJECT_ID, TestProjects.CHILD_TICKET_ID,
						new CreateTicketCommentRequest("Kommentar")));

		verify(ticketCommentRepository, never()).save(any());
	}

	@ParameterizedTest
	@MethodSource("invalidCreateTicketCommentRequests")
	void createTicketCommentRejectsInvalidRequestValues(CreateTicketCommentRequest request) {
		assertThrows(ValidationException.class,
				() -> ticketCommentService.createTicketComment(TestProjects.OWNER,
						TestProjects.PROJECT_ID, TestProjects.CHILD_TICKET_ID, request));

		verify(ticketRepository, never()).findByIdAndProjectId(anyLong(), anyLong());
		verify(ticketCommentRepository, never()).save(any());
	}

	private static TicketCommentModel ticketComment(long id, long ticketId, long authorId,
			String content) {
		final var comment = new TicketCommentModel();
		ReflectionTestUtils.setField(comment, "id", id);
		comment.setTicketId(ticketId);
		comment.setAuthorId(authorId);
		comment.setContent(content);
		comment.setCreatedAt(Instant.parse("2026-06-09T10:15:30Z"));
		comment.setUpdatedAt(Instant.parse("2026-06-09T10:15:30Z"));
		return comment;
	}

	private static Stream<CreateTicketCommentRequest> invalidCreateTicketCommentRequests() {
		return Stream.of(new CreateTicketCommentRequest(null), new CreateTicketCommentRequest(" "),
				new CreateTicketCommentRequest("a".repeat(10_001)));
	}
}
