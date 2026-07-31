package de.fallstudie.minerva.backend.common;

public class ReadOnlyException extends RuntimeException {
	public ReadOnlyException(String message) {
		super(message);
	}
}
