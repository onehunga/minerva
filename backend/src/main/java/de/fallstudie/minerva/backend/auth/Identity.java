package de.fallstudie.minerva.backend.auth;

import java.security.Principal;

public record Identity(long userId) implements Principal {
	@Override
	public String getName() {
		return String.valueOf(userId);
	}
}
