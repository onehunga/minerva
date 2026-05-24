package de.fallstudie.minerva.backend.authorization.internal.web;

import de.fallstudie.minerva.backend.authorization.internal.exception.InvalidUserException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class AuthorizationErrorHandler {

	@ExceptionHandler(InvalidUserException.class)
	@ResponseStatus(HttpStatus.FORBIDDEN)
	public String invalidUser() {
		return "Invalid user";
	}
}
