package de.fallstudie.minerva.backend.auth;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class AuthExceptionHandler {
	@ExceptionHandler(AuthenticationFailedException.class)
	ProblemDetail handleAuthenticationFailed(AuthenticationFailedException exception) {
		ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.UNAUTHORIZED);
		problemDetail.setDetail(exception.getMessage());
		return problemDetail;
	}

	@ExceptionHandler(DuplicateUsernameException.class)
	ProblemDetail handleDuplicateUsername(DuplicateUsernameException exception) {
		ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.CONFLICT);
		problemDetail.setDetail(exception.getMessage());
		return problemDetail;
	}
}
