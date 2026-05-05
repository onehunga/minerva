package de.fallstudie.minerva.backend.auth;

import lombok.experimental.UtilityClass;

@UtilityClass
public class RequestUtils {
	public String requireValue(String value, String message) {
		if (value == null || value.isBlank()) {
			throw new AuthenticationFailedException(message);
		}

		return value.trim();
	}
}
