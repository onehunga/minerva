package de.fallstudie.minerva.backend.realtime.internal.stream;

import java.util.List;

public record DataInvalidatedEvent(List<String> keys) {
}
