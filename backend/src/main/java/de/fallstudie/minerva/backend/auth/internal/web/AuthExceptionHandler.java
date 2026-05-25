package de.fallstudie.minerva.backend.auth.internal.web;

import de.fallstudie.minerva.backend.auth.internal.AuthenticationFailedException;
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
}
