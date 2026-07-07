package de.fallstudie.minerva.backend.ticket.internal.service;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import de.fallstudie.minerva.backend.common.ResourceNotFoundException;
import de.fallstudie.minerva.backend.common.ValidationException;
import de.fallstudie.minerva.backend.ticket.TicketEvent;
import de.fallstudie.minerva.backend.ticket.internal.persistence.TicketCommentModel;
import de.fallstudie.minerva.backend.ticket.internal.persistence.TicketCommentRepository;
import de.fallstudie.minerva.backend.ticket.internal.persistence.TicketRepository;
import de.fallstudie.minerva.backend.ticket.internal.web.CreateTicketCommentRequest;
import de.fallstudie.minerva.backend.ticket.internal.web.TicketCommentListResponse;
import de.fallstudie.minerva.backend.ticket.internal.web.TicketCommentResponse;
import de.fallstudie.minerva.backend.user.Identity;
import de.fallstudie.minerva.backend.user.UserService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TicketCommentService {
	private static final int MAX_CONTENT_LENGTH = 10_000;

	private final TicketRepository ticketRepository;
	private final TicketCommentRepository ticketCommentRepository;
	private final UserService userService;
	private final ApplicationEventPublisher eventPublisher;

	public TicketCommentListResponse getTicketComments(long projectId, long ticketId) {
		ensureTicketExists(projectId, ticketId);

		final var comments = ticketCommentRepository
				.findAllByTicketIdOrderByCreatedAtAscIdAsc(ticketId).stream()
				.map(this::toTicketCommentResponse).toList();

		return new TicketCommentListResponse(comments);
	}

	@Transactional
	public TicketCommentResponse createTicketComment(Identity identity, long projectId,
			long ticketId, CreateTicketCommentRequest request) {
		final var content = validateCreateTicketCommentRequest(request);
		ensureTicketExists(projectId, ticketId);

		final var comment = new TicketCommentModel();
		comment.setTicketId(ticketId);
		comment.setAuthorId(identity.userId());
		comment.setContent(content);

		final var savedComment = ticketCommentRepository.save(comment);
		eventPublisher.publishEvent(new TicketEvent.CommentCreated(identity.userId(), projectId,
				ticketId, savedComment.getId(), savedComment.getContent()));

		return toTicketCommentResponse(savedComment);
	}

	private void ensureTicketExists(long projectId, long ticketId) {
		ticketRepository.findByIdAndProjectId(ticketId, projectId)
				.orElseThrow(() -> new ResourceNotFoundException("Ticket nicht gefunden"));
	}

	private TicketCommentResponse toTicketCommentResponse(TicketCommentModel comment) {
		final var author = userService.findById(comment.getAuthorId())
				.orElseThrow(() -> new ResourceNotFoundException("Benutzer nicht gefunden"));

		return new TicketCommentResponse(comment.getId(), comment.getTicketId(),
				comment.getAuthorId(), author.username(), comment.getContent(),
				comment.getCreatedAt(), comment.getUpdatedAt());
	}

	private String validateCreateTicketCommentRequest(CreateTicketCommentRequest request) {
		if (request == null) {
			throw new IllegalArgumentException("Request must not be null");
		}

		if (request.content() == null || request.content().isBlank()) {
			throw new ValidationException("Kommentar darf nicht leer sein");
		}

		final var content = request.content().trim();
		if (content.length() > MAX_CONTENT_LENGTH) {
			throw new ValidationException("Kommentar darf maximal 10000 Zeichen lang sein");
		}

		return content;
	}
}
