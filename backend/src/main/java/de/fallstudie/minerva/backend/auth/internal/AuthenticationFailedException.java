package de.fallstudie.minerva.backend.auth.internal;

public class AuthenticationFailedException extends RuntimeException {
	public AuthenticationFailedException(String message) {
		super(message);
	}
}
