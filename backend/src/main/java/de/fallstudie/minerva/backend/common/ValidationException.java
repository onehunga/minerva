package de.fallstudie.minerva.backend.common;

public class ValidationException extends RuntimeException {
	public ValidationException(String message) {
		super(message);
	}
}
