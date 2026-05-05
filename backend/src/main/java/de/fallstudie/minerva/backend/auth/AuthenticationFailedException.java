package de.fallstudie.minerva.backend.auth;

public class AuthenticationFailedException extends RuntimeException {
	public AuthenticationFailedException(String message) {
		super(message);
	}
}
