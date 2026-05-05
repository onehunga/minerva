package de.fallstudie.minerva.backend.auth;

public class DuplicateUsernameException extends RuntimeException {
	public DuplicateUsernameException(String message) {
		super(message);
	}
}
