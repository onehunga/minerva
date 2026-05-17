package de.fallstudie.minerva.backend.common.internal.web;

import de.fallstudie.minerva.backend.common.DuplicateResourceException;
import de.fallstudie.minerva.backend.common.ValidationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
	@ExceptionHandler(ValidationException.class)
	ProblemDetail handleValidationException(ValidationException exception) {
		ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
		problemDetail.setDetail(exception.getMessage());
		return problemDetail;
	}

	@ExceptionHandler(DuplicateResourceException.class)
	ProblemDetail handleDuplicateResourceException(DuplicateResourceException exception) {
		ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.CONFLICT);
		problemDetail.setDetail(exception.getMessage());
		return problemDetail;
	}
}
